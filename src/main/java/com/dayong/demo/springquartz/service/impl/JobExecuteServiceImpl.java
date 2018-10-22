package com.dayong.demo.springquartz.service.impl;

import com.dayong.demo.springquartz.dao.JobDefinitionRepository;
import com.dayong.demo.springquartz.factory.DisallowScheduleFactory;
import com.dayong.demo.springquartz.factory.ScheduleFactory;
import com.dayong.demo.springquartz.pojo.JobDefinition;
import com.dayong.demo.springquartz.service.JobExecuteService;
import com.dayong.demo.springquartz.util.TaskUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 作业执行业务逻辑实现类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:39
 */
@Service
public class JobExecuteServiceImpl implements JobExecuteService {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private JobDefinitionRepository repository;

    /**
     * 获取单个任务
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    @Override
    public JobDefinition getJob(String jobName, String jobGroup) throws SchedulerException {
        JobDefinition job = null;
        Scheduler scheduler = getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (null != trigger) {
            job = createJob(jobName, jobGroup, scheduler, trigger);
        }

        return job;
    }

    private Scheduler getScheduler() {
        return schedulerFactoryBean.getScheduler();
    }

    private JobDefinition createJob(String jobName, String jobGroup, Scheduler scheduler, Trigger trigger)
            throws SchedulerException {
        JobDefinition job;
        job = new JobDefinition();
        job.setJobName(jobName);
        job.setJobGroup(jobGroup);
        job.setDescription("触发器:" + trigger.getKey());
        job.setNextTime(trigger.getNextFireTime());
        job.setPreviousTime(trigger.getPreviousFireTime());

        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
        job.setJobStatus(triggerState.name());

        if (trigger instanceof CronTrigger) {
            CronTrigger cronTrigger = (CronTrigger) trigger;
            String cronExpression = cronTrigger.getCronExpression();
            job.setCronExpression(cronExpression);
        }
        return job;
    }

    /**
     * 获取所有任务
     *
     * @return
     * @throws SchedulerException
     */
    @Override
    public List<JobDefinition> getAllJobs() throws SchedulerException {
        Scheduler scheduler = getScheduler();
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
        List<JobDefinition> jobList = new ArrayList<JobDefinition>();
        List<? extends Trigger> triggers;
        JobDefinition job;
        for (JobKey jobKey : jobKeys) {
            triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                job = createJob(jobKey.getName(), jobKey.getGroup(), scheduler, trigger);
                jobList.add(job);
            }
        }

        return jobList;
    }

    /**
     * 所有正在运行的job
     *
     * @return
     * @throws SchedulerException
     */
    @Override
    public List<JobDefinition> getRunningJob() throws SchedulerException {
        Scheduler scheduler = getScheduler();
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List<JobDefinition> jobList = new ArrayList<JobDefinition>(executingJobs.size());
        JobDefinition job;
        JobDetail jobDetail;
        JobKey jobKey;

        for (JobExecutionContext executingJob : executingJobs) {
            jobDetail = executingJob.getJobDetail();
            jobKey = jobDetail.getKey();

            job = createJob(jobKey.getName(), jobKey.getGroup(), scheduler, executingJob.getTrigger());
            jobList.add(job);
        }

        return jobList;
    }

    /**
     * 添加任务
     *
     * @param job
     * @throws SchedulerException
     */
    @Override
    public boolean addJob(JobDefinition job) throws SchedulerException {
        if (job == null || !JobDefinition.STATUS_RUNNING.equals(job.getJobStatus())) {
            return false;
        }

        String jobName = job.getJobName();
        String jobGroup = job.getJobGroup();
        String cronExpression = job.getCronExpression();
        if (!TaskUtils.isValidExpression(cronExpression)) {
            logger.error("时间表达式错误（" + jobName + "," + jobGroup + "）, " + cronExpression);
            return false;
        } else {
            Scheduler scheduler = getScheduler();
            // 任务名称和任务组设置规则：    // 名称：task_1 ..    // 组 ：group_1 ..
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            // 不存在，创建一个
            if (null == trigger) {
                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
                // 按新的表达式构建一个新的trigger
                trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                        .startAt(job.getStartTime() == null ? (new Date()) : job.getStartTime()) // 设置job不早于这个时间进行运行,和调用trigger的setStartTime方法效果一致
                        .withSchedule(scheduleBuilder).build();

                //是否允许并发执行
                JobDetail jobDetail = getJobDetail(job);
                // 将 job 信息存入数据库
                job.setStartTime(trigger.getStartTime());
                job.setNextTime(trigger.getNextFireTime());
                job.setPreviousTime(trigger.getPreviousFireTime());
                job = repository.save(job);
                jobDetail.getJobDataMap().put(getJobIdentity(job), job);

                scheduler.scheduleJob(jobDetail, trigger);

            } else { // trigger已存在，则更新相应的定时设置
                // 更新 job 信息到数据库
                job.setStartTime(trigger.getStartTime());
                job.setNextTime(trigger.getNextFireTime());
                job.setPreviousTime(trigger.getPreviousFireTime());
                job = repository.save(job);
                getJobDetail(job).getJobDataMap().put(getJobIdentity(job), job);

                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
                // 按新的表达式构建一个新的trigger
                trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                        .startAt(job.getStartTime() == null ? (new Date()) : job.getStartTime()) // 设置job不早于这个时间进行运行,和调用trigger的setStartTime方法效果一致
                        .withSchedule(scheduleBuilder).build();
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        }
        return true;
    }

    private String getJobIdentity(JobDefinition job) {
        return "jobDefinition" + (job.getJobGroup() + "_" + job.getJobName());
    }

    private JobDetail getJobDetail(JobDefinition job) {
        Class<? extends Job> clazz = JobDefinition.CONCURRENT_IS.equals(job.getIsConcurrent())
                ? ScheduleFactory.class : DisallowScheduleFactory.class;
        JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();
        return jobDetail;
    }

    /**
     * 暂停任务
     *
     * @param job
     * @return
     */
    @Override
    @Transactional
    public boolean pauseJob(JobDefinition job) {
        Scheduler scheduler = getScheduler();
        JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
        boolean result;
        try {
            scheduler.pauseJob(jobKey);

            // 更新任务状态到数据库
            job.setJobStatus(JobDefinition.STATUS_NOT_RUNNING);
            repository.modifyByStatus(job.getJobStatus(), job.getJobId());

            result = true;
        } catch (SchedulerException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 恢复任务
     *
     * @param job
     * @return
     */
    @Override
    @Transactional
    public boolean resumeJob(JobDefinition job) {
        Scheduler scheduler = getScheduler();
        JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
        boolean result;
        try {
            logger.info("resume job : " + (job.getJobGroup() + "_" + job.getJobName()));
            TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                    .startAt(job.getStartTime() == null ? (new Date()) : job.getStartTime()) // 设置job不早于这个时间进行运行,和调用trigger的setStartTime方法效果一致
                    .withSchedule(scheduleBuilder).build();
            scheduler.rescheduleJob(triggerKey, trigger);
            scheduler.resumeJob(jobKey);

            // 更新任务状态到数据库
            job.setJobStatus(JobDefinition.STATUS_RUNNING);
            repository.modifyByStatus(job.getJobStatus(), job.getJobId());

            result = true;
        } catch (SchedulerException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除任务
     */
    @Override
    @Transactional
    public boolean deleteJob(JobDefinition job) {
        Scheduler scheduler = getScheduler();
        JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
        boolean result;
        try {
            scheduler.deleteJob(jobKey);

            // 更新任务状态到数据库
            job.setJobStatus(JobDefinition.STATUS_DELETED);
            repository.modifyByStatus(job.getJobStatus(), job.getJobId());

            result = true;
        } catch (SchedulerException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 立即执行一个任务
     *
     * @param jobDefinition
     * @throws SchedulerException
     */
    @Override
    public void startJob(JobDefinition jobDefinition) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        JobKey jobKey = JobKey.jobKey(jobDefinition.getJobName(), jobDefinition.getJobGroup());
        scheduler.triggerJob(jobKey);
    }

    /**
     * 更新任务时间表达式
     *
     * @param job
     * @throws SchedulerException
     */
    @Override
    @Transactional
    public void updateCronExpression(JobDefinition job) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        //获取trigger，即在spring配置文件中定义的 bean id="myTrigger"
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        //表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        //按新的cronExpression表达式重新构建trigger
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
        //按新的trigger重新设置job执行
        scheduler.rescheduleJob(triggerKey, trigger);

        // 更新 job 信息到数据库
        job.setStartTime(trigger.getStartTime());
        job.setNextTime(trigger.getNextFireTime());
        job.setPreviousTime(trigger.getPreviousFireTime());
        job = repository.save(job);
        getJobDetail(job).getJobDataMap().put(getJobIdentity(job), job);
    }

    /**
     * 设置job的开始schedule时间
     *
     * @param job
     * @throws SchedulerException
     */
    @Override
    @Transactional
    public void updateStartTime(JobDefinition job) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        //获取trigger，即在spring配置文件中定义的 bean id="myTrigger"
        CronTriggerImpl trigger = (CronTriggerImpl) scheduler.getTrigger(triggerKey);
        trigger.setStartTime(job.getStartTime());
        //按新的trigger重新设置job执行
        scheduler.rescheduleJob(triggerKey, trigger);

        // 更新 job 信息到数据库
        job.setStartTime(trigger.getStartTime());
        job.setNextTime(trigger.getNextFireTime());
        job.setPreviousTime(trigger.getPreviousFireTime());
        job = repository.save(job);
        getJobDetail(job).getJobDataMap().put(getJobIdentity(job), job);
    }
}
