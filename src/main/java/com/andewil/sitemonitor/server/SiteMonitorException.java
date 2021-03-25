package com.andewil.sitemonitor.server;

public class SiteMonitorException extends RuntimeException {
    public SiteMonitorException() {
        super();
    }

    public SiteMonitorException(String message) {
        super(message);
    }

    public SiteMonitorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SiteMonitorException(Throwable cause) {
        super(cause);
    }
}
