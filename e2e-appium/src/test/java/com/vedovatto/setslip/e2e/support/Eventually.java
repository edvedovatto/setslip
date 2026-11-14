package com.vedovatto.setslip.e2e.support;

import static org.awaitility.Awaitility.await;

import com.vedovatto.setslip.e2e.config.TestConfig;
import java.time.Duration;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.ThrowingRunnable;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;

public final class Eventually {

    private Eventually() {}

    public static void eventually(ThrowingRunnable assertion) {
        assertWithin(polling().atMost(TestConfig.timeout()), TestConfig.timeout(), assertion);
    }

    public static void staysTrue(Duration period, ThrowingRunnable assertion) {
        Duration limit = period.plus(TestConfig.timeout());
        assertWithin(polling().during(period).atMost(limit), limit, assertion);
    }

    private static ConditionFactory polling() {
        return await().pollInSameThread()
                .pollInterval(TestConfig.POLL_INTERVAL)
                .ignoreExceptionsMatching(Eventually::isUiStillSettling);
    }

    private static void assertWithin(
            ConditionFactory condition, Duration limit, ThrowingRunnable assertion) {
        try {
            condition.untilAsserted(assertion);
        } catch (ConditionTimeoutException timeout) {
            if (timeout.getCause() instanceof AssertionError failure) {
                throw new AssertionError(
                        "Still failing after " + limit.toSeconds() + "s: " + failure.getMessage(),
                        failure);
            }
            throw timeout;
        }
    }

    private static boolean isUiStillSettling(Throwable error) {
        return error instanceof StaleElementReferenceException
                || error instanceof NoSuchElementException;
    }
}
