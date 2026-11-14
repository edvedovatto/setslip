package com.vedovatto.setslip.e2e.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public final class TestConfig {

    public static final String APP_PACKAGE = "com.vedovatto.setslip";
    public static final String APP_ACTIVITY = ".MainActivity";
    public static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    private TestConfig() {}

    public static URL appiumUrl() {
        String value = System.getProperty("appium.url", "http://127.0.0.1:4723");
        try {
            return URI.create(value).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalArgumentException("appium.url is not a valid URL: " + value, e);
        }
    }

    public static Optional<String> deviceUdid() {
        return property("device.udid");
    }

    public static Optional<Path> appPath() {
        return property("app.path").map(value -> Path.of(value).toAbsolutePath());
    }

    public static Duration timeout() {
        String value = System.getProperty("wait.seconds", "15");
        try {
            return Duration.ofSeconds(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("wait.seconds is not a whole number: " + value, e);
        }
    }

    private static Optional<String> property(String name) {
        return Optional.ofNullable(System.getProperty(name)).filter(value -> !value.isBlank());
    }
}
