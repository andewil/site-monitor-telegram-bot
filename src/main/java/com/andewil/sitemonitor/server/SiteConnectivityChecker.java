package com.andewil.sitemonitor.server;

public interface SiteConnectivityChecker {
    boolean checkSite(String url);
}
