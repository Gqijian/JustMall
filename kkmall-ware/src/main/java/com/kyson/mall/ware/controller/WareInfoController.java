package com.kyson.mall.ware.controller;

import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.R;
import com.kyson.mall.ware.entity.WareInfoEntity;
import com.kyson.mall.ware.service.WareInfoService;
import com.kyson.mall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 仓库信息
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 14:39:59
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {

    @Autowired
    private WareInfoService wareInfoService;

    /**
     * 获取运费
     * @param addrId
     * @return
     */
    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId){

        FareVo fare = wareInfoService.getFare(addrId);
        return R.ok().setData(fare);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params)
    {
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id)
    {
        WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareInfoEntity wareInfo)
    {
        wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareInfoEntity wareInfo)
    {
        wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids)
    {
        wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
