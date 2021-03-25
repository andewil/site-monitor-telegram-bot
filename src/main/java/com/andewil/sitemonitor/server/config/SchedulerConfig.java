package com.andewil.sitemonitor.server.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;

@Configuration
@Slf4j
public class SchedulerConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException, SchedulerException
    {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        //scheduler.setTriggers(jobOneTrigger(), jobTwoTrigger());
        //scheduler.setQuartzProperties(quartzProperties());
        //scheduler.setJobDetails(jobOneDetail(), jobTwoDetail());
        scheduler.setApplicationContextSchedulerContextKey("applicationContext");
        return scheduler;
    }


}
