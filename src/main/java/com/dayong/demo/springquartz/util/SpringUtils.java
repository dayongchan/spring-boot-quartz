package com.dayong.demo.springquartz.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring的工具类
 * @author Dayong Chan
 * @date 2018/10/22 17:49
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T getBean(String beanName) {
        if (applicationContext.containsBean(beanName)) {
            return (T) applicationContext.getBean(beanName);
        } else {
            return null;
        }
    }

    public static <T> T getBean(Class<T> beanCls) {
        return applicationContext.getBean(beanCls);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> baseType) {
        return applicationContext.getBeansOfType(baseType);
    }

    public static boolean isNotBlank(String str) {
        boolean result = true;
        if (null == str || "".equals(str.trim())) {
            result = false;
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }
}