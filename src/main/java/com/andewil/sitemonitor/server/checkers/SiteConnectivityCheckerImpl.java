package com.andewil.sitemonitor.server.checkers;

import com.andewil.sitemonitor.server.SiteConnectivityChecker;
import com.andewil.sitemonitor.server.SiteMonitorException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

@Slf4j
public class SiteConnectivityCheckerImpl implements SiteConnectivityChecker {
    @Override
    public boolean checkSite(String url) {
        try {
            URL checkUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) checkUrl.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(10);
            int code = connection.getResponseCode();
            if (code != 200) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new SiteMonitorException(e);
        }
    }
}
