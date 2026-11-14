package com.vedovatto.setslip.e2e.hooks;

import com.vedovatto.setslip.e2e.driver.Session;
import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.support.App;
import io.appium.java_client.android.AndroidDriver;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;

public class Hooks {

    private final PrinterScreen printer;

    public Hooks(PrinterScreen printer) {
        this.printer = printer;
    }

    @BeforeAll
    public static void openSession() {
        Session.start();
    }

    @AfterAll
    public static void closeSession() {
        Session.stop();
    }

    @Before
    public void startWithCleanApp() {
        App.startClean();
        printer.waitUntilOpen();
    }

    @After
    public void attachEvidenceOnFailure(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }
        try {
            AndroidDriver driver = Session.driver();
            scenario.attach(driver.getScreenshotAs(OutputType.BYTES), "image/png", "screen");
            scenario.attach(driver.getPageSource(), "text/plain", "view hierarchy");
        } catch (WebDriverException e) {
            scenario.log("Could not collect the screen after the failure: " + e.getMessage());
        }
    }
}
