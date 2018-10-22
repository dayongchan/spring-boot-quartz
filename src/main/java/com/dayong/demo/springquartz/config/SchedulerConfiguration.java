package com.dayong.demo.springquartz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * 调度定时器的配置
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:39
 */
@Configuration
public class SchedulerConfiguration {
    @Bean(name = "schedulerFactoryBean")
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        return schedulerFactoryBean;
    }
}
