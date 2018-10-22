package com.dayong.demo.springquartz.runner;

import com.dayong.demo.springquartz.pojo.JobDefinition;
import com.dayong.demo.springquartz.service.JobDefinitionService;
import com.dayong.demo.springquartz.service.JobExecuteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统启动完了之后启动定时调度作业的类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:42
 */
@Component
public class JobRunner implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JobExecuteService taskService;

    @Autowired
    private JobDefinitionService jobService;

    @Override
    public void run(String... args) throws Exception {
        // 可执行的任务列表
        List<JobDefinition> taskList = jobService.findByJobStatus(JobDefinition.STATUS_RUNNING);
        logger.info("初始化加载定时任务......");
        for (JobDefinition job : taskList) {
            try {
                taskService.addJob(job);
            } catch (Exception e) {
                logger.error("add job error: " + job.getJobName() + " " + job.getJobGroup(), e);
            }
        }
    }
}