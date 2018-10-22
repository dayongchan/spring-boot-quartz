package com.dayong.demo.springquartz.job;

import com.dayong.demo.springquartz.pojo.JobDefinition;
import com.dayong.demo.springquartz.service.JobExecuteService;
import com.dayong.demo.springquartz.util.SpringUtils;
import org.quartz.SchedulerException;

import java.util.List;

/**
 * 测试类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:44
 */
public class JobViewTest {
    private JobExecuteService taskService;

    public JobViewTest() {
        taskService = SpringUtils.getBean(JobExecuteService.class);
    }

    public void run() {
        List<JobDefinition> jobs;
        try {
            System.out.print("All jobs: ");
            jobs = taskService.getAllJobs();
            for (JobDefinition job : jobs) {
                System.out.print(job.getJobGroup() + "_" + job.getJobName() + " " + job.getJobStatus() + "\t");
            }
            System.out.println();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
