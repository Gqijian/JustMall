package com.kyson.mall.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.mall.product.dao.CategoryDao;
import com.kyson.mall.product.entity.CategoryEntity;
import com.kyson.mall.product.service.CategoryBrandRelationService;
import com.kyson.mall.product.service.CategoryService;
import com.kyson.mall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {

        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree()
    {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构
        List<CategoryEntity> level1Menu = entities.stream().filter((categoryEntity) ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList)
    {
        //TODO 检查当前要删除的菜单是否在别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId)
    {

        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     *
     * @CacheEvict 缓存失效模式 更新之后 直接删除缓存 重新录入
     * @CacheEvict(value = "catagory", allEntries = true)  指定删除某个分区下的所有数据
     * @param category
     */

    @Caching(evict = {
            //清除多个缓存
            @CacheEvict(value = "catagory", key = " 'getLevel1Category' "),
            @CacheEvict(value = "catagory", key = " 'getCataLogJson' ")
    })

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category)
    {

        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    //每一个需要缓存的我们都要指定放在哪个名字的缓存 【缓存分区（按照业务类型分）】
    @Cacheable(value = {"catagory"}, key = "#root.method.name", sync = true)  //当前方法结果需要缓存，如果缓存中有，方法不用调用。如果没有，就调用方法，将结果放入缓存
    @Override
    public List<CategoryEntity> getLevel1Category()
    {

        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "catagory", key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCataLogJson(){

        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //查出所有一级分类
        List<CategoryEntity> level1Category = getParentCid(selectList, 0L);
        Map<String, List<Catalog2Vo>> collect = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个一级分类 查到这个一级分类的子分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {

                    // 查找二级分类的 三级分类
                    List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                    List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                    if (level3Catalog != null) {

                        catalog3Vos = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString());

                            return catalog3Vo;
                        }).collect(Collectors.toList());


                    }

                    Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getCatId().toString(), l2.getName(), v.getCatId().toString(), catalog3Vos);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return collect;
    }


    public Map<String, List<Catalog2Vo>> getCataLogJson2()
    {

        /**
         * 1、设置空结果缓存：解决缓存穿透
         * 2、设置过期时间 （加随机值） 解决缓存雪崩
         * 3、加锁：解决缓存击穿 只让一个人查数据库 后面的走缓存
         */

        //加入缓存逻辑
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");

        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> cataLogJsonFromDb = getCataLogJsonFromDbWithRedissonLock();

            return cataLogJsonFromDb;
        }
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
        return result;
    }

    /**
     * 缓存里面的数据如何和数据库保持一致
     * 缓存数据一致性
     * 1、双写模式 ：就是写完数据库 马上去修改缓存
     * 2、失效模式：直接删除缓存
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCataLogJsonFromDbWithRedissonLock()
    {

        //占分布式锁，去redis占坑
        //锁的名字 锁的粒度越细越快，锁的力度越大，锁的资源越多，也就越慢
        //锁的粒度：具体缓存的是某个数据 11-号商品； product-11-lock product-12-lock
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb(); //如果这里出异常，会导致 锁没有删除，形成死锁问题

        } finally {
            lock.unlock();
        }

        return dataFromDb;

    }

    public Map<String, List<Catalog2Vo>> getCataLogJsonFromDbWithRedisLock()
    {

        String token = UUID.randomUUID().toString();
        //占用分布式锁 去redis占坑
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", token, 300, TimeUnit.SECONDS);
        if (lock) {
            //加锁成功 数据库执行业务

            //但是如果在这里断电，依然会形成死锁 这里操作不是 原子的
            //redisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb(); //如果这里出异常，会导致 锁没有删除，形成死锁问题

            } finally {
                // 假如 redisTemplate.opsForValue().get("lock") 时，网络反应较长，那么，redis 中锁已经自动删除，但是 还是返回是 自己的token 这样就会导致误删别人的锁

                String script = "if redis.call(get,KEYS[1]) == ARGV[1]" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                //删除锁
                Long delRes = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock", token));
            }

            //获取对比值+对比成功删除 也得是原子操作。
            //String lockValue = redisTemplate.opsForValue().get("lock");
            //if(token.equals(lockValue)){
            //    redisTemplate.delete("lock");   //释放锁
            //}

            return dataFromDb;

        } else {
            //加锁失败  重试 synchronized() 自旋锁
            //休眠 100 ms
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }

            return getCataLogJsonFromDbWithRedisLock(); //自旋
        }

    }

    private Map<String, List<Catalog2Vo>> getDataFromDb()
    {
        //得到锁之后，应该再去缓存中确定一次
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }

        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //查出所有一级分类
        List<CategoryEntity> level1Category = getParentCid(selectList, 0L);
        Map<String, List<Catalog2Vo>> collect = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个一级分类 查到这个一级分类的子分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {

                    // 查找二级分类的 三级分类
                    List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                    List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                    if (level3Catalog != null) {

                        catalog3Vos = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString());

                            return catalog3Vo;
                        }).collect(Collectors.toList());


                    }

                    Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getCatId().toString(), l2.getName(), v.getCatId().toString(), catalog3Vos);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        //将查到的数据转为 json 放入缓存
        String toJSONString = JSON.toJSONString(collect);
        redisTemplate.opsForValue().set("catalogJSON", toJSONString);
        return collect;
    }

    /**
     * 从数据库查询并封装分类数据
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCataLogJsonFromDbWithLocalLock()
    {

        /**
         * 只要是同一把锁，就能锁住需要这个锁的所有线程
         *
         * 1、synchronized (this) SpringBoot 的所有容器组件都是单例的
         */

        //TODO 本地锁 synchronized JUC(lock) 在分布式情况下 想要锁住所有 必须使用分布式锁
        synchronized (this) {
            //得到锁之后，应该再去缓存中确定一次
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)) {
                Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
                return result;
            }

            List<CategoryEntity> selectList = baseMapper.selectList(null);
            //查出所有一级分类
            List<CategoryEntity> level1Category = getParentCid(selectList, 0L);
            Map<String, List<Catalog2Vo>> collect = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                //每一个一级分类 查到这个一级分类的子分类
                List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
                List<Catalog2Vo> catalog2Vos = null;
                if (categoryEntities != null) {
                    catalog2Vos = categoryEntities.stream().map(l2 -> {

                        // 查找二级分类的 三级分类
                        List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                        if (level3Catalog != null) {

                            catalog3Vos = level3Catalog.stream().map(l3 -> {
                                Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString());

                                return catalog3Vo;
                            }).collect(Collectors.toList());


                        }

                        Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getCatId().toString(), l2.getName(), v.getCatId().toString(), catalog3Vos);
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                }
                return catalog2Vos;
            }));
            //将查到的数据转为 json 放入缓存
            String toJSONString = JSON.toJSONString(collect);
            redisTemplate.opsForValue().set("catalogJSON", toJSONString);
            return collect;
        }
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> categoryEntities, Long parentCid)
    {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));

        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths)
    {

        paths.add(catelogId);

        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }


    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all)
    {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map((categoryEntity) -> {
            //找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}