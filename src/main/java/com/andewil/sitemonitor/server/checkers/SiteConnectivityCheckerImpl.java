package com.andewil.sitemonitor.server.checkers;

import com.andewil.sitemonitor.server.SiteConnectivityChecker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

@Slf4j
public class SiteConnectivityCheckerImpl implements SiteConnectivityChecker {
    @Override
    public String checkSite(String url) {
        try {
            URL checkUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) checkUrl.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(10);
            connection.setReadTimeout(30);
            connection.setRequestProperty("User-Agent", "Site-Monitor-Checker/1.1");
            int code = connection.getResponseCode();
            log.trace("{}: ContentType: {}", url, connection.getContentType());
            return String.valueOf(code);
        } catch (MalformedURLException e) {
            return "MalformedURLException: " + e.getMessage();
        } catch (IOException e) {
            return "IOException: " + e.getMessage();
        }
    }
}
