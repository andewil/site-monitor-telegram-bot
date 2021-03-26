package com.andewil.sitemonitor.server.mappers;

import com.andewil.sitemonitor.server.models.SiteCheckRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class SiteCheckMapper implements RowMapper<SiteCheckRecord> {

    @Override
    public SiteCheckRecord mapRow(ResultSet resultSet, int i) throws SQLException {
        SiteCheckRecord item = new SiteCheckRecord();
        item.setCheckData(resultSet.getString("data"));
        item.setCheckResult(resultSet.getString("check_result"));
        item.setCheckTime(OffsetDateTime.now());
        item.setId(resultSet.getInt("check_id"));
        item.setSiteId(resultSet.getInt("site_id"));
        return item;
    }
}
