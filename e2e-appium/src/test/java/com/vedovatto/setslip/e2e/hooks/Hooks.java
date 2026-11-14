package com.vedovatto.setslip.e2e.hooks;

import com.vedovatto.setslip.e2e.driver.Session;
import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.support.App;
import com.vedovatto.setslip.e2e.support.Device;
import com.vedovatto.setslip.e2e.support.Evidence;
import io.appium.java_client.android.AndroidDriver;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import java.nio.charset.StandardCharsets;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.opentest4j.TestAbortedException;

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

    @Before(value = "@animations", order = 1)
    public void skipWhenAnimationsAreOff() {
        if (Device.animatorScale().filter(scale -> scale == 0f).isPresent()) {
            throw new TestAbortedException(
                    "Animations are turned off on the device and this scenario follows the print animation");
        }
    }

    @Before
    public void startWithCleanApp() {
        Evidence.startScenario();
        App.startClean();
        printer.waitUntilOpen();
    }

    @After
    public void attachEvidenceOnFailure(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                attachEvidence(scenario);
            } else {
                Evidence.discardVideo();
            }
        } catch (WebDriverException e) {
            scenario.log("Could not collect evidence after the scenario: " + e.getMessage());
        }
    }

    private static void attachEvidence(Scenario scenario) {
        AndroidDriver driver = Session.driver();
        scenario.attach(driver.getScreenshotAs(OutputType.BYTES), "image/png", "screen");
        scenario.attach(driver.getPageSource(), "text/plain", "view hierarchy");
        scenario.attach(Evidence.logcat().getBytes(StandardCharsets.UTF_8), "text/plain", "logcat");
        Evidence.video()
                .ifPresent(video -> scenario.attach(video, "video/mp4", "screen recording"));
    }
}
