package com.dayong.demo.springquartz.service.impl;

import com.dayong.demo.springquartz.dao.JobExecuteResultRepository;
import com.dayong.demo.springquartz.pojo.JobExecuteResult;
import com.dayong.demo.springquartz.service.JobExecuteResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 作业执行结果业务逻辑实现类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:54
 */
@Service("dtsResultService")
public class JobExecuteResultServiceImpl implements JobExecuteResultService {

    @Autowired
    private JobExecuteResultRepository repository;

    @Override
    public JobExecuteResult save(JobExecuteResult result) {
        return repository.save(result);
    }
}
