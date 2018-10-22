package com.dayong.demo.springquartz.service;

import com.dayong.demo.springquartz.pojo.JobDefinition;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @author Dayong Chan
 * @date 2018/10/22 17:32
 */
public interface JobDefinitionService {
    List<JobDefinition> findAll();

    @Transactional
    JobDefinition save(JobDefinition jobBean);

    JobDefinition getOne(long jobId);

    @Transactional
    int modifyByIdAndTime(Date previousTime, Date nextTime, Long jobId);

    List<JobDefinition> findByJobStatus(String jobStatus);

    List<JobDefinition> findByJobStatusNot(String jobStatus);

    @Transactional
    int modifyByStatus(String jobStatus, Long jobId);
}
