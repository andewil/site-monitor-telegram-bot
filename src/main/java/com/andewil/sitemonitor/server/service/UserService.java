package com.andewil.sitemonitor.server.service;

import com.andewil.sitemonitor.server.SiteMonitorException;
import com.andewil.sitemonitor.server.mappers.UserMapper;
import com.andewil.sitemonitor.server.models.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserRecord getUser(long chatId) {
        try {
            String q = "select * from sys_user u where u.telegram_id=?";
            return getJdbcTemplate().queryForObject(q, new UserMapper(), chatId);
        } catch (Exception e) {
            throw new SiteMonitorException(String.format("Get user (chatId=%d) failed", chatId), e);
        }
    }

    public UserRecord getUser(UUID userId) {
        try {
            String q = "select * from sys_user u where u.user_id=?";
            return getJdbcTemplate().queryForObject(q, new UserMapper(), userId);
        } catch (Exception e) {
            throw new SiteMonitorException(String.format("Get user (id=%s) failed", userId.toString()), e);
        }
    }

    public boolean isUserExists(long chatId) {
        try {
            String q = "select count(*) as res from sys_user u where u.telegram_id=?";
            int res = getJdbcTemplate().queryForObject(q, Integer.class, chatId);
            return res > 0;
        } catch (Exception e) {
            throw new SecurityException("isUserExists failed", e);
        }
    }

    public UserRecord addUser(UserRecord userRecord) {
        try {
            String q = "insert into sys_user(user_id, telegram_id, chat_mode, command, created, last_update) select ?, ?, ?, ?, now(), now()";
            jdbcTemplate.update(q,
                    userRecord.getUserId(),
                    userRecord.getChatId(),
                    userRecord.getChatMode().name(),
                    userRecord.getCommand());
            return getUser(userRecord.getChatId());
        } catch (Exception e) {
            throw new SiteMonitorException("Exception of add user", e);
        }
    }

    private JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

    public void updateCommand(long chatId, String command) {
        try {
            String q = "update sys_user set command=?, last_update=now() where telegram_id=?";
            jdbcTemplate.update(q,
                    command,
                    chatId);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception of update command", e);
        }
    }

    public void updateChatMode(long chatId, UserRecord.ChatMode mode) {
        try {
            String q = "update sys_user set chat_mode=?, last_update=now()  where telegram_id=?";
            jdbcTemplate.update(q,
                    mode.name(),
                    chatId);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception of update chat mode", e);
        }
    }
}
