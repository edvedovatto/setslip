package com.vedovatto.setslip.e2e.screens;

import com.vedovatto.setslip.e2e.config.TestConfig;
import com.vedovatto.setslip.e2e.driver.Session;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumFluentWait;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AppiumWebElement;
import io.appium.java_client.support.ui.FluentWait;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public abstract class Screen {

    protected static final By BACK = tagged("btn_back");

    private static final int LONG_PRESS_MILLIS = 900;
    private static final By TEXT = AppiumBy.className("android.widget.TextView");

    protected static By tagged(String tag) {
        return AppiumBy.id(tag);
    }

    protected static By taggedWithPrefix(String prefix) {
        return AppiumBy.androidUIAutomator(tagMatching(prefix + ".+"));
    }

    protected static By enabledWithPrefix(String prefix) {
        return enabledMatching(prefix + ".+");
    }

    protected static By enabledMatching(String pattern) {
        return AppiumBy.androidUIAutomator(tagMatching(pattern) + ".enabled(true)");
    }

    protected static By checkedWithPrefix(String prefix) {
        return AppiumBy.androidUIAutomator(tagMatching(prefix + ".+") + ".checked(true)");
    }

    protected static By inside(String containerTag, String tag) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\""
                        + containerTag
                        + "\").childSelector(new UiSelector().resourceId(\""
                        + tag
                        + "\"))");
    }

    private static String tagMatching(String pattern) {
        return "new UiSelector().resourceIdMatches(\"" + pattern + "\")";
    }

    protected AndroidDriver driver() {
        return Session.driver();
    }

    protected FluentWait<AndroidDriver> waitingFor(String outcome) {
        return new AppiumFluentWait<>(driver())
                .withTimeout(TestConfig.timeout())
                .pollingEvery(TestConfig.POLL_INTERVAL)
                .withMessage("waiting for " + outcome)
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
    }

    protected WebElement shown(By locator) {
        return waitingFor(locator + " to show")
                .until(
                        driver -> {
                            WebElement element = driver.findElement(locator);
                            return element.isDisplayed() ? element : null;
                        });
    }

    protected void waitUntilGone(By locator) {
        waitingFor(locator + " to go away")
                .until(
                        driver ->
                                driver.findElements(locator).stream()
                                        .noneMatch(WebElement::isDisplayed));
    }

    protected void tap(By locator) {
        waitingFor(locator + " to accept a tap")
                .until(
                        driver -> {
                            WebElement element = driver.findElement(locator);
                            return element.isDisplayed() && element.isEnabled() ? element : null;
                        })
                .click();
    }

    protected void longPress(By locator) {
        AppiumWebElement element = (AppiumWebElement) shown(locator);
        driver().executeScript(
                        "mobile: longClickGesture",
                        Map.of("elementId", element.getId(), "duration", LONG_PRESS_MILLIS));
    }

    protected void scrollTo(String tag) {
        By scrolledIntoView =
                AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true))"
                                + ".scrollIntoView(new UiSelector().resourceId(\""
                                + tag
                                + "\"))");
        waitingFor(tag + " to scroll into view")
                .until(driver -> driver.findElement(scrolledIntoView));
    }

    protected boolean isPresent(By locator) {
        return count(locator) > 0;
    }

    protected int count(By locator) {
        return driver().findElements(locator).size();
    }

    protected String text(By locator) {
        return attribute(locator, "text");
    }

    protected Optional<String> textIfPresent(By locator) {
        return driver().findElements(locator).stream().findFirst().map(WebElement::getText);
    }

    protected String description(By locator) {
        return attribute(locator, "content-desc");
    }

    protected boolean isEnabled(By locator) {
        return Boolean.parseBoolean(attribute(locator, "enabled"));
    }

    protected boolean isChecked(By locator) {
        return Boolean.parseBoolean(attribute(locator, "checked"));
    }

    protected String attribute(By locator, String name) {
        return Optional.ofNullable(driver().findElement(locator).getAttribute(name)).orElse("");
    }

    protected Rectangle bounds(By locator) {
        return driver().findElement(locator).getRect();
    }

    protected List<String> tags(By locator) {
        return driver().findElements(locator).stream()
                .map(element -> element.getAttribute("resource-id"))
                .toList();
    }

    protected List<String> textsInside(By container) {
        return driver().findElement(container).findElements(TEXT).stream()
                .map(WebElement::getText)
                .toList();
    }
}
