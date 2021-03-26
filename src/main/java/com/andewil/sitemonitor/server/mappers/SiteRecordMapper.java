package com.andewil.sitemonitor.server.mappers;

import com.andewil.sitemonitor.server.models.SiteRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SiteRecordMapper implements RowMapper<SiteRecord> {
    @Override
    public SiteRecord mapRow(ResultSet resultSet, int i) throws SQLException {
        SiteRecord record = new SiteRecord();
        record.setId(resultSet.getInt("site_id"));
        record.setCheckInterval(resultSet.getInt("check_interval"));
        record.setEnabled(resultSet.getBoolean("is_enabled"));
        record.setUrl(resultSet.getString("url"));
        record.setUserId(resultSet.getObject("user_id", UUID.class));
        record.setNaturalId(resultSet.getInt("natural_id"));
        record.setLastResult(resultSet.getString("last_result"));
        return record;
    }
}
