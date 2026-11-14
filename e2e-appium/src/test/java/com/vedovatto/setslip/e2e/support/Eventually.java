package com.vedovatto.setslip.e2e.support;

import static org.awaitility.Awaitility.await;

import com.vedovatto.setslip.e2e.config.TestConfig;
import java.time.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;

public final class Eventually {

    private Eventually() {}

    public static void eventually(ThrowingRunnable assertion) {
        await().pollInSameThread()
                .atMost(TestConfig.timeout())
                .pollInterval(TestConfig.POLL_INTERVAL)
                .ignoreExceptionsMatching(Eventually::isUiStillSettling)
                .untilAsserted(assertion);
    }

    public static void staysTrue(Duration period, ThrowingRunnable assertion) {
        await().pollInSameThread()
                .during(period)
                .atMost(period.plus(TestConfig.timeout()))
                .pollInterval(TestConfig.POLL_INTERVAL)
                .ignoreExceptionsMatching(Eventually::isUiStillSettling)
                .untilAsserted(assertion);
    }

    private static boolean isUiStillSettling(Throwable error) {
        return error instanceof StaleElementReferenceException
                || error instanceof NoSuchElementException;
    }
}
