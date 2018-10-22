package com.dayong.demo.springquartz.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 测试类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:43
 */
public class JobTest {
    public void run() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println(df.format(new Date()) + " job executing...");
        //throw new NullPointerException("sss");//测试抛异常的情况看会不会继续执行【结果是还会继续重复执行】
    }
}
