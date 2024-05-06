package com.kyson.mall.product.feign.fallback;


import com.kyson.common.exception.BizCodeEnum;
import com.kyson.common.utils.R;
import com.kyson.mall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecKillFeignServiceFallBack implements SeckillFeignService {

    @Override
    public R getSkuSeckillInfo(Long skuId)
    {
        log.info("熔断方法调用。。。");
        return R.error(BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
    }
}
