package com.dayong.demo.springquartz.controller;

import com.dayong.demo.springquartz.pojo.JobDefinition;
import com.dayong.demo.springquartz.service.JobDefinitionService;
import com.dayong.demo.springquartz.service.JobExecuteService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页的控制器
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:45
 */
@Controller
public class IndexController {

    @Autowired
    private JobDefinitionService jobService;

    @Autowired
    private JobExecuteService taskService;

    @GetMapping("/")
    public String getJobList(Model model) {
        List<JobDefinition> jobList = jobService.findByJobStatusNot(JobDefinition.STATUS_DELETED);
        model.addAttribute("jobs", jobList);
        return "jobList";
    }

    @PutMapping("/{id}/status")
    @ResponseBody
    public List<JobDefinition> updateJobStatus(@PathVariable("id") Long id, @RequestParam("jobStatus") String jobStatus, Model model) {
        jobService.modifyByStatus(jobStatus, id);
        JobDefinition jobBean = jobService.getOne(id);

        List<JobDefinition> jobs;
        try {
            jobs = taskService.getAllJobs();
            if (JobDefinition.STATUS_RUNNING.equals(jobBean.getJobStatus()) && !jobs.contains(jobBean)) {
                taskService.addJob(jobBean);
            } else if (JobDefinition.STATUS_RUNNING.equals(jobBean.getJobStatus()) && jobs.contains(jobBean)) {
                taskService.resumeJob(jobBean);
            }

            if (JobDefinition.STATUS_NOT_RUNNING.equals(jobBean.getJobStatus()) && jobs.contains(jobBean)) {
                taskService.pauseJob(jobBean);
            } else if (JobDefinition.STATUS_NOT_RUNNING.equals(jobBean.getJobStatus()) && !jobs.contains(jobBean)) {
                jobService.modifyByStatus(jobBean.getJobStatus(), jobBean.getJobId());
            }
        } catch (SchedulerException e1) {
            e1.printStackTrace();
        }

        List<JobDefinition> jobList = jobService.findByJobStatusNot(JobDefinition.STATUS_DELETED);
        return jobList;
    }

    @PutMapping("/{id}/date")
    @ResponseBody
    public Map<String, String> updateNextRunDate(@PathVariable("id") Long id, @RequestParam("date") String date, Model model) {
        JobDefinition jobBean = jobService.getOne(id);
        if (jobBean != null && date != null) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startTime;
            try {
                startTime = df.parse(date);
                jobBean.setStartTime(startTime);
                jobBean.setNextTime(startTime);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            jobService.save(jobBean);
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("status", "success");
        return result;
    }
}