package com.dayong.demo.springquartz.dao;

import com.dayong.demo.springquartz.pojo.JobExecuteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 作业执行结果数据库操作类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:55
 */
@Repository
public interface JobExecuteResultRepository extends JpaRepository<JobExecuteResult, Long> {

}