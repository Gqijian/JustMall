package com.kyson.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kyson<br />
 * @description: <br/>
 * @date: 2022/8/29 14:10<br/>
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = new HashSet<>();

    /**
     * 初始化方法
     *
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation)
    {
        int[] values = constraintAnnotation.values();

        for (int val : values)
        {
            set.add(val);
        }

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * 判断是否校验成功
     *
     * @param value   需要校验的值
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext)
    {
        return set.contains(value);
    }
}
