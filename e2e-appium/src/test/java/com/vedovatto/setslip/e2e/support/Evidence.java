package com.vedovatto.setslip.e2e.support;

import com.vedovatto.setslip.e2e.config.TestConfig;
import com.vedovatto.setslip.e2e.driver.Session;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openqa.selenium.logging.LogEntry;

public final class Evidence {

    private static final String LOGCAT = "logcat";
    private static final int LOGCAT_LINES_KEPT = 500;
    private static final Duration LONGEST_RECORDING = Duration.ofMinutes(3);

    private static boolean recording;

    private Evidence() {}

    public static void startScenario() {
        logcatSinceLastRead();
        recording = false;
        if (TestConfig.recordVideo()) {
            Session.driver()
                    .startRecordingScreen(
                            new AndroidStartScreenRecordingOptions()
                                    .withTimeLimit(LONGEST_RECORDING)
                                    .enableForcedRestart());
            recording = true;
        }
    }

    public static String logcat() {
        List<LogEntry> entries = logcatSinceLastRead();
        return entries.stream()
                .skip(Math.max(0, entries.size() - LOGCAT_LINES_KEPT))
                .map(LogEntry::toString)
                .collect(Collectors.joining("\n"));
    }

    public static Optional<byte[]> video() {
        if (!recording) {
            return Optional.empty();
        }
        recording = false;
        byte[] video = Base64.getMimeDecoder().decode(Session.driver().stopRecordingScreen());
        return video.length == 0 ? Optional.empty() : Optional.of(video);
    }

    public static void discardVideo() {
        video();
    }

    private static List<LogEntry> logcatSinceLastRead() {
        return Session.driver().manage().logs().get(LOGCAT).getAll();
    }
}
