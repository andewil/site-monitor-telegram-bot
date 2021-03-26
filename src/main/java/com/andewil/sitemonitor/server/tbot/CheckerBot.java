package com.andewil.sitemonitor.server.tbot;

import com.andewil.sitemonitor.server.SiteMonitorException;
import com.andewil.sitemonitor.server.models.SiteRecord;
import com.andewil.sitemonitor.server.models.UserRecord;
import com.andewil.sitemonitor.server.service.SchedulerService;
import com.andewil.sitemonitor.server.service.SiteService;
import com.andewil.sitemonitor.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CheckerBot extends TelegramLongPollingBot {
    public static final String ADD_SITE_CHECK = "addcheck";
    public static final String DELETE_SITE_CHECK = "deletecheck";
    public static final String LIST_CHECKS = "listchecks";
    public static final String LIST_JOBS = "listjobs";
    public static final String LAST_RESULTS = "lastresults";
    public static final String HELP = "help";
    public static final String START = "start";

    @Value("${tbot.username}")
    private String tbotUserName;
    @Value("${tbot.token}")
    private String tbotToken;

    private final UserService userService;
    private final SiteService siteService;
    private final SchedulerService schedulerService;
    private final Scheduler scheduler;

    @Override
    public String getBotUsername() {
        return tbotUserName;
    }

    @Override
    public String getBotToken() {
        return tbotToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Message received: {}", update);
        if (update.hasMessage()) {
            handleUpdateWithMessage(update);
        }
    }

    /**
     * Решистрация бота
     */
    public void register() {
        String shToken = tbotToken.substring(0, 12).concat("...");
        log.info("registering the bot with params (name: {}, token: {})", tbotUserName, shToken);
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Bot registering exception", e);
        }
    }

    private void handleUpdateWithMessage(Update update) {
        // register user
        UserRecord user = checkUserRegistration(update.getMessage());

        Message message = update.getMessage();
        log.debug("{} handling message: {}", getPrefixString(update.getMessage()), message.getText());
        if (isCommand(message.getText())) {
            handleCommand(update, message, user);
        } else if (user.getChatMode()== UserRecord.ChatMode.INPUT) {
            handleInput(message, user);
        }

    }

    private boolean isCommand(String s) {
        return s.startsWith("/");
    }

    private void handleCommand(Update update, Message message, UserRecord user) {
        String text = message.getText().toLowerCase();
        boolean isKnownCommand = true;
        int commandEndPosition = text.indexOf(" ");
        if (commandEndPosition < 0) {
            commandEndPosition = text.length();
        }
        String command = text.substring(1, commandEndPosition);
        log.info("{} command: '{}'", getPrefixString(message), command);
        switch (command) {
            case ADD_SITE_CHECK:
                handleCmdAddSite(message, user);
                break;
            case LIST_CHECKS:
                handleCmdListChecks(message, user);
                break;
            case DELETE_SITE_CHECK:
                handleCmdDeleteSite(message, user);
                break;
            case LIST_JOBS:
                handleCmdListJobs(message, user);
                break;
            case LAST_RESULTS:
                handleCmdLastResults(message, user);
                break;
            case HELP:
                handleCmdHelp(message, user);
                break;
            case START:
                handleCmdHelp(message, user);
                break;
            default:
                isKnownCommand = false;
                log.error("Unknown command: '{}'", command);
                sendMessageAsHTML(user.getChatId(), "Unknown command: " + command);
        }

        if (isKnownCommand) {
            userService.updateCommand(user.getChatId(), command);
        }
    }

    private void handleCmdStart(Message message, UserRecord user) {
        log.info("{} start", getPrefixString(message));
    }

    /**
     * Отправка сообщения без TelegramApiException. Вместо него будет вызвано RuntimeException
     *
     * @param sm SendMessage object
     */
    private void sendMessage(SendMessage sm) {
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new SiteMonitorException("Ошибка при отправке сообщения", e);
        }
    }

    /**
     * Отправка сообщения без TelegramApiException. Вместо него будет вызвано RuntimeException
     *
     */
    public void sendMessageAsHTML(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText(text);
        sm.enableHtml(true);
        sm.disableWebPagePreview();
        sendMessage(sm);
    }

    private String getPrefixString(Message message) {
        return "[" + String.valueOf(message.getChatId()) + "][" + message.getFrom().getUserName() + "(" + message.getFrom().getLastName() + " " + message.getFrom().getFirstName() + ")]";
    }

    private void handleCmdAddSite(Message message, UserRecord user) {
        log.info("{} add site check", getPrefixString(message));
        sendMessageAsHTML(user.getChatId(), "Input site URL (example: https://google.com)");
        userService.updateChatMode(user.getChatId(), UserRecord.ChatMode.INPUT);
    }

    private void handleCmdDeleteSite(Message message, UserRecord user) {
        log.info("{} delete site check", getPrefixString(message));
        String stringBuilder = "Input ID of a check to delete\n\n" +
                getSiteCheckList(user);
        sendMessageAsHTML(user.getChatId(), stringBuilder);
        userService.updateChatMode(user.getChatId(), UserRecord.ChatMode.INPUT);
    }

    private void handleCmdListJobs(Message message, UserRecord user) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("current jobs\n\n");
            List<JobExecutionContext> jobList = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext item: jobList) {
                stringBuilder.append(item.getJobDetail().getKey() + " : " + item.getJobDetail().getDescription() + "\n");
            }
            sendMessageAsHTML(user.getChatId(), stringBuilder.toString());
        } catch (SchedulerException e) {
            log.error("get job names failed", e);
        }
    }

    private void handleCmdHelp(Message message, UserRecord user) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Commands</b>\n\n");
        stringBuilder.append("<b>/listchecks</b> - list of checks\n");
        stringBuilder.append("<b>/addcheck</b> - add a new check\n");
        stringBuilder.append("<b>/deletecheck</b> - remove a check\n");
        stringBuilder.append("<b>/lastresults</b> - get last check results\n");
        sendMessageAsHTML(user.getChatId(), stringBuilder.toString());
    }

    private UserRecord checkUserRegistration(Message message) {
        long chatId = message.getChatId();
        if (userService.isUserExists(chatId)) {
            return userService.getUser(chatId);
        } else {
            UserRecord userRecord = new UserRecord();
            userRecord.setUserId(UUID.randomUUID());
            userRecord.setChatId(chatId);
            userRecord.setCommand("start");
            return userService.addUser(userRecord);
        }
    }

    private void handleInput(Message message, UserRecord user) {
        switch (user.getCommand()) {
            case ADD_SITE_CHECK:
                handleInputAddSiteCheck(message, user);
                break;
            case DELETE_SITE_CHECK:
                handleInputDeleteSiteCheck(message, user);
                break;
            default:
                log.error("No handlers for input command: {}", user.getCommand());
        }
    }

    private void handleInputAddSiteCheck(Message message, UserRecord user) {
        try {
            SiteRecord siteRecord = new SiteRecord();
            siteRecord.setUserId(user.getUserId());
            siteRecord.setUrl(message.getText());
            siteRecord.setEnabled(true);
            siteRecord = siteService.addSite(siteRecord);
            schedulerService.addSiteCheck(siteRecord);
            sendMessageAsHTML(user.getChatId(), "Site (" + siteRecord.getUrl() + ") added to monitoring");
            userService.updateChatMode(message.getChatId(), UserRecord.ChatMode.MENU);
        } catch (SiteMonitorException | SchedulerException e) {
            log.error("Exception when add site", e);
            sendMessageAsHTML(user.getChatId(), "Exception: " + e.getMessage());
        }
    }

    private void handleInputDeleteSiteCheck(Message message, UserRecord user) {
        try {
            String s = message.getText();
            int naturalId = Integer.parseInt(s);
            SiteRecord siteRecord = siteService.getSite(user.getUserId(), naturalId);
            schedulerService.removeSiteCheck(siteRecord);
            siteService.deleteSite(siteRecord.getId());
            sendMessageAsHTML(user.getChatId(), "Site (" + siteRecord.getUrl() + ") deleted from monitoring");
            userService.updateChatMode(message.getChatId(), UserRecord.ChatMode.MENU);
        } catch (SiteMonitorException e) {
            log.error("Exception when delete site", e);
            sendMessageAsHTML(user.getChatId(), "Exception: " + e.getMessage());
        }
    }

    private void handleCmdListChecks(Message message, UserRecord user) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Lists of sites</b>\n\n");
        stringBuilder.append(getSiteCheckList(user));
        sendMessageAsHTML(user.getChatId(), stringBuilder.toString());
    }

    private void handleCmdLastResults(Message message, UserRecord user) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Lists of sites</b>\n\n");
        stringBuilder.append(getLastResults(user));
        sendMessageAsHTML(user.getChatId(), stringBuilder.toString());
    }

    private String getSiteCheckList(UserRecord user) {
        List<SiteRecord> listChecks = siteService.getAllForUser(user.getUserId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" No  | int| URL\n");
        for (SiteRecord item: listChecks) {
            stringBuilder.append(String.format("%1$5d | %2$3d | %3$s", item.getNaturalId(), item.getCheckInterval(), item.getUrl()))
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    private String getLastResults(UserRecord user) {
        List<SiteRecord> listChecks = siteService.getAllForUser(user.getUserId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" No : URL : result\n");
        for (SiteRecord item: listChecks) {
            stringBuilder.append(String.format("%1$5d | %2$s | %3$s", item.getNaturalId(), item.getUrl(), item.getLastResult()))
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
