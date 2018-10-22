package com.dayong.demo.springquartz.dao;

import com.dayong.demo.springquartz.pojo.JobDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 作业定义数据库操作类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:31
 */
@Repository
public interface JobDefinitionRepository extends JpaRepository<JobDefinition, Long> {

    List<JobDefinition> findByJobStatus(String jobStatus);

    List<JobDefinition> findByJobStatusNot(String jobStatus);

    // 修改上一次执行时间和下一次执行时间
    @Modifying
    @Query("update JobDefinition j set j.previousTime = ?1, j.nextTime = ?2 where j.jobId = ?3")
    int modifyByIdAndTime(Date previousTime, Date nextTime, Long jobId);

    // 修改job状态
    @Modifying
    @Query("update JobDefinition j set j.jobStatus = ?1 where j.jobId = ?2")
    int modifyByStatus(String jobStatus, Long jobId);

}