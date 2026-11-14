package com.vedovatto.setslip.e2e.driver;

import com.vedovatto.setslip.e2e.config.TestConfig;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import java.time.Duration;

public final class Session {

    private static AndroidDriver driver;

    private Session() {}

    public static void start() {
        UiAutomator2Options options =
                new UiAutomator2Options()
                        .setAppPackage(TestConfig.APP_PACKAGE)
                        .setAppActivity(TestConfig.APP_ACTIVITY)
                        .setNoReset(true)
                        .setNewCommandTimeout(Duration.ofSeconds(120));
        options.setCapability("appium:settings[waitForIdleTimeout]", 0);
        options.setCapability("appium:settings[disableIdLocatorAutocompletion]", true);
        TestConfig.deviceUdid().ifPresent(options::setUdid);
        TestConfig.appPath().ifPresent(path -> options.setApp(path.toString()));

        driver = new AndroidDriver(TestConfig.appiumUrl(), options);
    }

    public static AndroidDriver driver() {
        if (driver == null) {
            throw new IllegalStateException("The Appium session has not been started");
        }
        return driver;
    }

    public static void stop() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
