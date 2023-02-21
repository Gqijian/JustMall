package com.kyson.mall.product.web;

import com.kyson.mall.product.entity.CategoryEntity;
import com.kyson.mall.product.service.CategoryService;
import com.kyson.mall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <dependency>
 * <groupId>org.springframework.boot</groupId>
 * <artifactId>spring-boot-devtools</artifactId>
 * <optional>true</optional>
 * </dependency>
 * <p>
 * 不重启项目 刷新页面 导入 devtools 并且 optional 为 true
 * 并且在 yml 中关闭 thymeleaf 的缓存 然后在编译器页面中 ctrl + f9 / ctrl + shift + f9 自动编译下页面
 */

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redis;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model)
    {

        List<CategoryEntity> categoryEntities = categoryService.getLevel1Category();

        //视图解析器进行拼串
        //默认前缀 classpath:/templates/ + 返回值 +  默认后缀 .html
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson()
    {

        Map<String, List<Catalog2Vo>> map = categoryService.getCataLogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public String hello()
    {

        //1、获取一把锁，只要锁名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        //加锁
        //1、锁的自动续期 如果业务超长，运行期间自动给锁续上 30s 。不用担心业务时间长，锁被自动删掉
        //如果我们未指定锁的超时时间，就使用 30*1000 LockWatchdogTimeOut 看门狗默认时间
        // 只要未指定，就会启动一个定时任务 重新给锁设置过期时间，新的过期时间 就是看门狗默认时间的三分之一
        lock.lock();

        //如果我们传递了锁的超时时间，就发送给redis 执行脚本，进行占锁，默认超时就是我们指定的时间
        lock.lock(10, TimeUnit.SECONDS);    //如果设置自动解锁，自动解锁时间一定要大于业务执行时间

        try {
            //加锁成功执行业务
            System.out.println("业务 " + Thread.currentThread().getId());
            Thread.sleep(30000);
        }catch (Exception e){}finally {
            System.out.println("解锁 " + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    /**
     * 读写锁 保证一定能读到最新数据
     *
     * 读 + 读 相当于无锁，只会在redis 中记录，所有当前读锁，都会加锁成功
     * 写 + 读 等待写锁释放
     * 写 + 写 阻塞方式
     * 读 + 写 有读锁，写也需要等待
     * @return
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue()
    {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        rLock.lock();

        String s = "";
        try {

            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redis.opsForValue().set("uuid", s);

        }catch (Exception e){}finally {
            rLock.unlock();
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue()
    {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        rLock.lock();

        String s = "";
        try {

            redis.opsForValue().get("uuid");
            Thread.sleep(30000);
        }catch (Exception e){}finally {
            rLock.unlock();
        }
        return s;
    }

    /**
     * 车库停车 三辆车 信号量
     * @return
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException
    {

        RSemaphore park = redisson.getSemaphore("park");

        park.acquire(); //获取一个信号 / 获取一个值 占一个车位
        park.tryAcquire();  //试着获取 有就停 没有就算了

        return "ok";
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() throws InterruptedException
    {

        RSemaphore park = redisson.getSemaphore("park");

        park.release(); // 释放一个车位

        return "ok";
    }

    /**
     * 放假锁门
     * 1 班没人了，2。。
     * 5 个班全部走完，才可以锁大门
     * @return
     */
    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException
    {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();   //等闭锁都完成

        return "放假了";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id){

        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();   //计数减一
        return id + "走了";
    }
}
