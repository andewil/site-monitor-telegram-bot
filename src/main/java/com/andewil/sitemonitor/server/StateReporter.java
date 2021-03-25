package com.andewil.sitemonitor.server;

import java.util.UUID;

public interface StateReporter {
    void reportStateChanged(UUID userId, int siteId, String prevState, String newState, String message);
}
