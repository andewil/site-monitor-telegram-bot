package com.andewil.sitemonitor.server.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SiteRecord {
    private int id;
    private String url;
    private int checkInterval = 60;
    private UUID userId;
    private boolean isEnabled = true;
    private int naturalId;
}
