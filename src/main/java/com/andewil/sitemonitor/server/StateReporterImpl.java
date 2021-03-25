package com.andewil.sitemonitor.server;

import com.andewil.sitemonitor.server.models.SiteRecord;
import com.andewil.sitemonitor.server.models.UserRecord;
import com.andewil.sitemonitor.server.service.SiteService;
import com.andewil.sitemonitor.server.service.UserService;
import com.andewil.sitemonitor.server.tbot.CheckerBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StateReporterImpl implements StateReporter {
    private final CheckerBot checkerBot;
    private final UserService userService;
    private final SiteService siteService;

    @Override
    public void reportStateChanged(UUID userId, int siteId, String prevState, String currentState, String message) {
        UserRecord user =userService.getUser(userId);
        SiteRecord siteRecord = siteService.getSite(siteId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Site check result changed (" + siteRecord.getUrl() + ")\r\n")
                .append(" previous value : " + prevState + "\r\n")
                .append(" current value  : " + currentState + "\r\n");
        checkerBot.sendMessageAsHTML(user.getChatId(), stringBuilder.toString());
    }
}
