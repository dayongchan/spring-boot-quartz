package com.dayong.demo.springquartz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

/**
 * JPA的配置
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:41
 */
@Configuration
public class JPAConfiguration {

    @Bean
    public OpenEntityManagerInViewFilter openEntityManagerInViewFilter() {
        return new OpenEntityManagerInViewFilter();
    }
}
