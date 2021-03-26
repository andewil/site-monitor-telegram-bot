package com.andewil.sitemonitor.server;

import com.andewil.sitemonitor.server.checkers.SiteConnectivityCheckerImpl;
import com.andewil.sitemonitor.server.config.DbConfig;
import com.andewil.sitemonitor.server.models.SiteRecord;
import com.andewil.sitemonitor.server.service.SchedulerService;
import com.andewil.sitemonitor.server.service.SiteService;
import com.andewil.sitemonitor.server.tbot.CheckerBot;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class Starter {
    private final DbConfig dbConfig;
    private final SchedulerService schedulerService;
    private final SiteService siteService;
    private final CheckerBot checkerBot;

    public Starter(DbConfig dbConfig, SchedulerService schedulerService, CheckerBot checkerBot, SiteService siteService) {
        this.dbConfig = dbConfig;
        this.schedulerService = schedulerService;
        this.checkerBot = checkerBot;
        this.siteService = siteService;

        try {
            schedulerService.init();
            schedulerService.start();
        } catch (SchedulerException e) {
            throw new SiteMonitorException("Scheduler initialization exception. Probably the botToken is not specified.", e);
        }

        directCheck();
        //jobCheck();

        try {
            log.info("registering telegram bot...");
            checkerBot.register();
            log.info("bot registered");
        } catch (Exception e) {
            throw new SiteMonitorException("Bot register exception", e);
        }

        startChecks();
    }

    private void directCheck() {
        // check google
        log.info("check google...");
        SiteConnectivityChecker checker = new SiteConnectivityCheckerImpl();
        String checkResult = checker.checkSite("https://google.com");
        log.info(" result: {}", checkResult);
    }

    private void jobCheck() {
        // check 2
        SiteRecord siteRecord = new SiteRecord();
        siteRecord.setUrl("https://google.com");
        siteRecord.setCheckInterval(5);
        siteRecord.setId(0);

        try {
            schedulerService.addSiteCheck(siteRecord);
        } catch (Exception e) {
            throw new SiteMonitorException("schedule exception", e);
        }
    }

    private void startChecks() {
        List<SiteRecord> records = siteService.getAllEnabled();
        for (SiteRecord item: records) {
            try {
                schedulerService.addSiteCheck(item);
            } catch (SchedulerException e) {
                log.error("can not add scheduler site check", e);
            }
        }
    }
}
