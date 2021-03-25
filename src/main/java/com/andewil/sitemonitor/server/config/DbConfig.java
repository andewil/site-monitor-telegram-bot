package com.andewil.sitemonitor.server.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Slf4j
@RequiredArgsConstructor
@Getter
public class DbConfig {

    @Value("${spring.datasource.url}")
    String dbUrl;
    @Value("${spring.datasource.username}")
    String dbUser;
    @Value("${spring.datasource.password}")
    String dbPassword;
    @Value("${spring.datasource.driver-class-name}")
    String driverClassName;
    private DataSource dataSource;

    @Primary
    @Bean
    public DataSource dataSource() {
        if (dataSource != null) return dataSource;

        log.info("init database connection...");
        log.info(" driver name       : {}", driverClassName);
        log.info(" database url      : {}", dbUrl);
        log.info(" database user     : {}", dbUser);
        log.info(" database password : [hidden]");

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .password(dbPassword)
                .username(dbUser)
                .url(dbUrl);
        dataSource = dataSourceBuilder.build();

        try {
            try (Connection cn = dataSource.getConnection()) {
                log.info(" RDBMS      : {} ", cn.getMetaData().getDatabaseProductName());
                log.info(" version    : {}", cn.getMetaData().getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            log.error("Can't obtain database version");
        }
        log.info("connected");

        return dataSource;
    }

}
