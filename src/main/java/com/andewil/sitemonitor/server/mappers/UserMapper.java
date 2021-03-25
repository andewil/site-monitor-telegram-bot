package com.andewil.sitemonitor.server.mappers;

import com.andewil.sitemonitor.server.models.UserRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserMapper implements RowMapper<UserRecord> {
    @Override
    public UserRecord mapRow(ResultSet resultSet, int i) throws SQLException {
        UserRecord userRecord = new UserRecord();
        userRecord.setChatId(resultSet.getLong("telegram_id"));
        userRecord.setUserId(resultSet.getObject("user_id", UUID.class));
        userRecord.setChatMode(UserRecord.parseChatMode(resultSet.getString("chat_mode")));
        userRecord.setCommand(resultSet.getString("command"));
        return userRecord;
    }

}
