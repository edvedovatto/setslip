package com.vedovatto.setslip.e2e.support;

import com.vedovatto.setslip.e2e.config.TestConfig;
import com.vedovatto.setslip.e2e.driver.Session;
import io.appium.java_client.android.AndroidDriver;
import java.util.Map;

public final class App {

    private App() {}

    public static void startClean() {
        AndroidDriver driver = Session.driver();
        driver.terminateApp(TestConfig.APP_PACKAGE);
        driver.executeScript("mobile: clearApp", Map.of("appId", TestConfig.APP_PACKAGE));
        driver.activateApp(TestConfig.APP_PACKAGE);
    }

    public static void restart() {
        AndroidDriver driver = Session.driver();
        driver.terminateApp(TestConfig.APP_PACKAGE);
        driver.activateApp(TestConfig.APP_PACKAGE);
    }

    public static void pressBack() {
        Session.driver().navigate().back();
    }

    public static boolean inForeground() {
        return TestConfig.APP_PACKAGE.equals(Session.driver().getCurrentPackage());
    }
}
