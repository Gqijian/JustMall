package com.kyson.mall.product.exception;

import com.kyson.common.exception.BizCodeEnum;
import com.kyson.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kyson <br />
 * @description: 集中处理所有异常
 * @date: 2022/8/26 16:21<br/>
 */

//@ResponseBody   //异常全部json 写出
//@ControllerAdvice(basePackages = "com.kyson.mall.product.controller")
@Slf4j
@RestControllerAdvice(basePackages = "com.kyson.mall.product.controller")
public class KkMallExceptionControllerAdvice {

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable)
    {
        log.error("出现了未知异常: ", throwable);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e)
    {
        log.error("异常类型：{}, 数据校验出现问题: {} ", e.getClass(), e.getMessage());

        BindingResult errors = e.getBindingResult();
        Map<String, String> map = new HashMap<>();
        errors.getFieldErrors().forEach((fieldError) -> {

            //获取到错误属性名字
            String field = fieldError.getField();

            //FieldError 获取到错误提示
            String defaultMessage = fieldError.getDefaultMessage();

            map.put(field, defaultMessage);
        });
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg()).put("data", map);
    }
}
