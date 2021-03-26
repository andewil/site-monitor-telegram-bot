package com.andewil.sitemonitor.server.models;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SiteCheckRecord {
    private int id;
    private int siteId;
    private String checkResult;
    private String checkData;
    private OffsetDateTime checkTime;
}
