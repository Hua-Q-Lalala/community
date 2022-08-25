package com.hua.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 线程池配置类 用于测试spring线程池， 与项目业务无直接联系
 * @create 2022-05-13 12:45
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPollConfig {
}
