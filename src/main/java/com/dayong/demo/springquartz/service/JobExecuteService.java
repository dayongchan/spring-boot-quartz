package com.dayong.demo.springquartz.service;

import com.dayong.demo.springquartz.pojo.JobDefinition;
import org.quartz.SchedulerException;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Dayong Chan
 * @date 2018/10/22 17:39
 */
public interface JobExecuteService {


    /**
     * 获取单个任务
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    JobDefinition getJob(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 获取所有任务
     *
     * @return
     * @throws SchedulerException
     */
    List<JobDefinition> getAllJobs() throws SchedulerException;

    /**
     * 所有正在运行的job
     *
     * @return
     * @throws SchedulerException
     */
    List<JobDefinition> getRunningJob() throws SchedulerException;

    /**
     * 添加任务
     *
     * @param job
     * @throws SchedulerException
     */
    boolean addJob(JobDefinition job) throws SchedulerException;

    /**
     * 暂停任务
     *
     * @param job
     * @return
     */
    @Transactional
    boolean pauseJob(JobDefinition job);

    /**
     * 恢复任务
     *
     * @param job
     * @return
     */
    @Transactional
    boolean resumeJob(JobDefinition job);

    /**
     * 删除任务
     */
    @Transactional
    boolean deleteJob(JobDefinition job);

    /**
     * 立即执行一个任务
     *
     * @param jobDefinition
     * @throws SchedulerException
     */
    void startJob(JobDefinition jobDefinition) throws SchedulerException;

    /**
     * 更新任务时间表达式
     *
     * @param job
     * @throws SchedulerException
     */
    @Transactional
    void updateCronExpression(JobDefinition job) throws SchedulerException;

    /**
     * 设置job的开始schedule时间
     *
     * @param job
     * @throws SchedulerException
     */
    @Transactional
    void updateStartTime(JobDefinition job) throws SchedulerException;
}
