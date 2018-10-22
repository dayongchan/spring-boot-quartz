package com.dayong.demo.springquartz.factory;

import com.dayong.demo.springquartz.pojo.JobDefinition;
import com.dayong.demo.springquartz.pojo.JobExecuteResult;
import com.dayong.demo.springquartz.service.JobDefinitionService;
import com.dayong.demo.springquartz.service.JobExecuteResultService;
import com.dayong.demo.springquartz.util.SpringUtils;
import com.dayong.demo.springquartz.util.TaskUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Job有状态实现类，不允许并发执行
 * 若一个方法一次执行不完下次轮转时则等待该方法执行完后才执行下一次操作
 * 主要是通过注解：@DisallowConcurrentExecution (quartz 2.3.0 版本中 StatefulJob 已经被弃用，改用注解的方式)
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:41
 */
@DisallowConcurrentExecution
public class DisallowScheduleFactory implements Job {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail job = context.getJobDetail();
        JobKey key = job.getKey();
        String jobIdentity = "jobDefinition" + key.getGroup() + "_" + key.getName();
        Trigger trigger = context.getTrigger();
        JobDefinition jobDefinition = (JobDefinition) context.getMergedJobDataMap().get(jobIdentity);
        logger.info("运行任务名称 = [" + jobDefinition + "]");

        JobExecuteResult result = new JobExecuteResult();
        try {
            result.setJobId(jobDefinition.getJobId());
            result.setCreateTime(new Date());
            boolean invoke = TaskUtils.invokeMethod(jobDefinition.getJobClass(), jobDefinition.getMethodName(), null);
            if (invoke) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                result.setErrorMsg("执行失败");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        jobDefinition.setNextTime(trigger.getNextFireTime());
        jobDefinition.setPreviousTime(trigger.getPreviousFireTime());
        JobDefinitionService jobService = SpringUtils.getBean(JobDefinitionService.class);
        jobService.modifyByIdAndTime(jobDefinition.getPreviousTime(), jobDefinition.getNextTime(), jobDefinition.getJobId());

        // 写入运行结果
        JobExecuteResultService dtsService = SpringUtils.getBean(JobExecuteResultService.class);
        dtsService.save(result);
    }
}
