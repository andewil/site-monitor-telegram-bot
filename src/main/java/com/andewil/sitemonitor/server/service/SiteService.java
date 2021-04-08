package com.andewil.sitemonitor.server.service;

import com.andewil.sitemonitor.server.SiteMonitorException;
import com.andewil.sitemonitor.server.mappers.SiteRecordMapper;
import com.andewil.sitemonitor.server.models.SiteCheckRecord;
import com.andewil.sitemonitor.server.models.SiteRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLType;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class SiteService {
    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate getTemplate() {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

    public List<SiteRecord> getAllEnabled() {
        String q = "select * from site where is_enabled=true";
        return getTemplate().query(q, new SiteRecordMapper());
    }

    public List<SiteRecord> getAllForUser(UUID userId) {
        String q = "select * from site where user_id=? and is_enabled=true order by natural_id";
        return getTemplate().query(q, new SiteRecordMapper(), userId);
    }

    public SiteRecord getSite(int id) {
        try {
            String q = "select * from site where site_id=?";
            return getTemplate().queryForObject(q, new SiteRecordMapper(), id);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when getting site record", e);
        }
    }

    public SiteRecord getSite(UUID userId, int naturalId) {
        try {
            String q = "select * from site where user_id=? and natural_id=?";
            return getTemplate().queryForObject(q, new SiteRecordMapper(), userId, naturalId);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when getting site record", e);
        }
    }

    public SiteRecord addSite(SiteRecord siteRecord) {
        try {
            Integer lastNaturalId = getLastNaturalId(siteRecord.getUserId());
            int naturalId;
            if (lastNaturalId == null) {
                naturalId = 1;
            } else {
                naturalId = lastNaturalId + 1;
            }

            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(getTemplate());
            simpleJdbcInsert.withTableName("site").usingGeneratedKeyColumns("site_id");
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("url", siteRecord.getUrl())
                    .addValue("user_id", siteRecord.getUserId())
                    .addValue("is_enabled", siteRecord.isEnabled())
                    .addValue("check_interval", siteRecord.getCheckInterval())
                    .addValue("natural_id", naturalId);
            Number id = simpleJdbcInsert.executeAndReturnKey(parameterSource);
            return getSite(id.intValue());
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when adding site record", e);
        }
    }

    public void deleteSite(int id) {
        try {
            String q = "update site set is_enabled=false where site_id=?";
            getTemplate().update(q, id);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when delete site", e);
        }
    }

    public void updateCheckInterval(int id, int interval) {
        try {
            String q = "update site set check_interval=? where site_id=?";
            getTemplate().update(q, interval, id);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when updateCheckInterval", e);
        }
    }

    public Integer getLastNaturalId(UUID userId) {
        try {
            String q = "select max(natural_id) as res from site where user_id=?";
            return getTemplate().queryForObject(q, Integer.class, userId);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when getLastNaturalId", e);
        }
    }

    public void updateLastResult(int siteId, String lastResult) {
        try {
            String q = "update site set last_result=?, updated_time=now() where site_id=?";
            getTemplate().update(q, lastResult, siteId);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when getLastNaturalId", e);
        }
    }

    public String getLastResult(int siteId) {
        try {
            String q = "select last_result from site where site_id=?";
            return getTemplate().queryForObject(q, String.class, siteId);
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when getLastResult", e);
        }
    }

    public boolean isLastResultChanged(int siteId, String currentValue) {
        String lastResult = getLastResult(siteId);
        return (!currentValue.equals(lastResult));
    }

    public int addCheckResult(SiteCheckRecord record) {
        try {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(getTemplate());
            simpleJdbcInsert.withTableName("site_check").usingGeneratedKeyColumns("check_id");
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("site_id", record.getSiteId())
                    .addValue("check_result", record.getCheckResult())
                    .addValue("data", record.getCheckData())
                    .addValue("check_date", record.getCheckTime(), Types.TIMESTAMP_WITH_TIMEZONE)
                    ;
            Number id = simpleJdbcInsert.executeAndReturnKey(parameterSource);
            return id.intValue();
        } catch (Exception e) {
            throw new SiteMonitorException("Exception occurred when adding site check record", e);
        }
    }
}
