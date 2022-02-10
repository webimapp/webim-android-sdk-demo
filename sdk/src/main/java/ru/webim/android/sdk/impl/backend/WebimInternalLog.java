package ru.webim.android.sdk.impl.backend;

import androidx.annotation.Nullable;

import ru.webim.android.sdk.Webim.SessionBuilder.WebimLogVerbosityLevel;
import ru.webim.android.sdk.WebimLog;
import ru.webim.android.sdk.WebimLogEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Nikita Kaberov on 01.02.18.
 */

public class WebimInternalLog {
    private static final WebimInternalLog ourInstance = new WebimInternalLog();
    @Nullable
    private WebimLog logger;
    @Nullable
    private WebimLogVerbosityLevel verbosityLevel;
    private String lastActionResponseJSON = "";
    private String lastDeltaResponseJSON = "";
    private Set<WebimLogEntity> logEntities = new HashSet<>();

    public void setLastActionResponseJSON(String json) {
        lastActionResponseJSON = json;
    }

    public void setLastDeltaResponseJSON(String json) {
        lastDeltaResponseJSON = json;
    }

    public static WebimInternalLog getInstance() {
        return ourInstance;
    }

    private WebimInternalLog() {
    }

    public void setLogger(@Nullable WebimLog logger) {
        this.logger = logger;
    }

    public void setVerbosityLevel(@Nullable WebimLogVerbosityLevel verbosityLevel) {
        this.verbosityLevel = verbosityLevel;
    }

    public void setLogEntities(Set<WebimLogEntity> logEntities) {
        this.logEntities = logEntities;
    }

    void logResponse(String log, WebimLogVerbosityLevel verbosityLevel) {
        if (log.contains(WebimService.URL_SUFFIX_DELTA)) {
            log += System.getProperty("line.separator") + lastDeltaResponseJSON;
            lastDeltaResponseJSON = "";
        } else {
            log += System.getProperty("line.separator") + lastActionResponseJSON;
            lastActionResponseJSON = "";
        }
        log(log, verbosityLevel, WebimLogEntity.SERVER);
    }

    public void log(String log, WebimLogVerbosityLevel verbosityLevel) {
        log(log, verbosityLevel, WebimLogEntity.SERVER);
    }

    public void log(String log, WebimLogVerbosityLevel verbosityLevel, WebimLogEntity logArea) {
        if (logger != null && verbosityLevel != null) {
            if (logEntities.contains(logArea)) {
                logInternal(log, verbosityLevel);
            }
        }
    }

    private void logInternal(String log, WebimLogVerbosityLevel verbosityLevel) {
        log = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS z", Locale.getDefault()).format(new Date())
            + " "
            + levelToString(verbosityLevel)
            + "WEBIM LOG: "
            + System.getProperty("line.separator")
            + log;
        switch (verbosityLevel) {
            case VERBOSE:
                if (isVerbose()) {
                    this.logger.log(log);
                }
                break;
            case DEBUG:
                if (isDebug()) {
                    this.logger.log(log);
                }
                break;
            case INFO:
                if (isInfo()) {
                    this.logger.log(log);
                }
                break;
            case WARNING:
                if (isWarning()) {
                    this.logger.log(log);
                }
                break;
            case ERROR:
                this.logger.log(log);
                break;
        }
    }

    private boolean isVerbose() {
        return this.verbosityLevel.equals(WebimLogVerbosityLevel.VERBOSE);
    }

    private boolean isDebug() {
        return this.verbosityLevel.equals(WebimLogVerbosityLevel.DEBUG) || this.isVerbose();
    }

    private boolean isInfo() {
        return this.verbosityLevel.equals(WebimLogVerbosityLevel.INFO) || this.isDebug();
    }

    private boolean isWarning() {
        return this.verbosityLevel.equals(WebimLogVerbosityLevel.WARNING) || this.isInfo();
    }

    private String levelToString(WebimLogVerbosityLevel level) {
        switch (level) {
            case VERBOSE:
                return "V/";
            case DEBUG:
                return "D/";
            case INFO:
                return "I/";
            case WARNING:
                return "W/";
            case ERROR:
                return "E/";
        }
        return "";
    }
}
