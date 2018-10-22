package com.dayong.demo.springquartz.service.impl;

import com.dayong.demo.springquartz.dao.JobDefinitionRepository;
import com.dayong.demo.springquartz.pojo.JobDefinition;
import com.dayong.demo.springquartz.service.JobDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * 作业定义业务逻辑实现类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:34
 */
@Service
public class JobDefinitionServiceImpl implements JobDefinitionService {
    @Autowired
    private JobDefinitionRepository repository;

    @Override
    public List<JobDefinition> findAll() {
        return repository.findAll();
    }

    @Transactional
    @Override
    public JobDefinition save(JobDefinition jobBean) {
        return repository.save(jobBean);
    }

    @Override
    public JobDefinition getOne(long jobId) {
        return repository.getOne(jobId);
    }

    @Transactional
    @Override
    public int modifyByIdAndTime(Date previousTime, Date nextTime, Long jobId) {
        return repository.modifyByIdAndTime(previousTime, nextTime, jobId);
    }

    @Override
    public List<JobDefinition> findByJobStatus(String jobStatus) {
        return repository.findByJobStatus(jobStatus);
    }

    @Override
    public List<JobDefinition> findByJobStatusNot(String jobStatus) {
        return repository.findByJobStatusNot(jobStatus);
    }


    @Transactional
    @Override
    public int modifyByStatus(String jobStatus, Long jobId) {
        return repository.modifyByStatus(jobStatus, jobId);
    }
}
