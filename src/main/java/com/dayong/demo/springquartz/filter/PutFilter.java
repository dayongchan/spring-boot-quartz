package com.dayong.demo.springquartz.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.HttpPutFormContentFilter;

/**
 * Http请求过滤器，用于开启PUT请求
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:45
 */
@Component
public class PutFilter extends HttpPutFormContentFilter {

}