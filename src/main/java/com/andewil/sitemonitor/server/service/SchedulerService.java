package com.andewil.sitemonitor.server.service;

import com.andewil.sitemonitor.server.SiteMonitorException;
import com.andewil.sitemonitor.server.jobs.SiteConnctivityJob;
import com.andewil.sitemonitor.server.models.SiteRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SchedulerService {
    public static final String SITE_CHECK = "SiteCheck_";
    //private final SchedulerFactory schedulerFactory;
    private final Scheduler scheduler;

    public void init() throws SchedulerException {
        //schedulerFactory = new StdSchedulerFactory();
        //scheduler = schedulerFactory.getScheduler();
    }

    public void addSiteCheck(SiteRecord siteRecord) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("url", siteRecord.getUrl());
        dataMap.put("siteId", siteRecord.getId());
        dataMap.put("userId", siteRecord.getUserId().toString());

        JobDetail job = JobBuilder.newJob(SiteConnctivityJob.class)
                .withDescription(siteRecord.getUrl())
                .withIdentity(SITE_CHECK + String.format("%1$5s", siteRecord.getId()).replace(' ', '0'))
                .setJobData(dataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(SITE_CHECK + siteRecord.getId(), "USER_" + siteRecord.getUserId().toString())
                .withSchedule(
                        SimpleScheduleBuilder
                                .simpleSchedule()
                                .withIntervalInSeconds(siteRecord.getCheckInterval())
                        .repeatForever()
                )
                .build();

        scheduler.scheduleJob(job, trigger);

        log.info("Check added: {} (interval={}): {}", siteRecord.getId(), siteRecord.getCheckInterval(), siteRecord.getUrl());
    }

    public void removeSiteCheck(SiteRecord siteRecord) {
        try {
            scheduler.unscheduleJob(new TriggerKey(SITE_CHECK + siteRecord.getId(), "USER_" + siteRecord.getUserId().toString()));
        } catch (SchedulerException e) {
            throw new SiteMonitorException("Exception when delete site check trigger", e);
        }
    }

    public void start() throws SchedulerException {
        scheduler.start();
    }
}
