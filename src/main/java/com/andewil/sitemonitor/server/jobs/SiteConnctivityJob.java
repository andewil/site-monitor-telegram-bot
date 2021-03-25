package com.andewil.sitemonitor.server.jobs;

import com.andewil.sitemonitor.server.SiteConnectivityChecker;
import com.andewil.sitemonitor.server.StateReporter;
import com.andewil.sitemonitor.server.checkers.SiteConnectivityCheckerImpl;
import com.andewil.sitemonitor.server.service.SiteService;
import com.andewil.sitemonitor.server.tbot.CheckerBot;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class SiteConnctivityJob implements Job {
    private SiteService siteService;
    private StateReporter stateReporter;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            // get parameters
            JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
            String url = dataMap.getString("url");
            String s = dataMap.getString("userId");
            UUID userId = UUID.fromString(s);
            int siteId = dataMap.getInt("siteId");
            String prefix = "[" + jobExecutionContext.getJobDetail().getKey() + "][" + url + "]";
            // get beans
            ApplicationContext applicationContext = (ApplicationContext) jobExecutionContext.getScheduler().getContext().get("applicationContext");
            siteService = applicationContext.getBean(SiteService.class);
            stateReporter = applicationContext.getBean(StateReporter.class);
            // get state
            String prevState = siteService.getLastResult(siteId);
            boolean prevStateBoolean = Boolean.parseBoolean(prevState);
            boolean result;
            try {
                SiteConnectivityChecker checker = new SiteConnectivityCheckerImpl();
                result = checker.checkSite(url);
                log.debug("{} result: {}", prefix, result);
            } catch (Exception e) {
                result = false;
                siteService.updateLastResult(siteId, "false");
                log.debug("{} result: false; exception: {}", prefix, e.getMessage());
            }

            if (prevStateBoolean != result) {
                stateReporter.reportStateChanged(userId, siteId, prevState, String.valueOf(result), "State is changed");
            }
            siteService.updateLastResult(siteId, String.valueOf(result));
        } catch (SchedulerException e) {
            log.error("Scheduler exception", e);
        }
    }

}
