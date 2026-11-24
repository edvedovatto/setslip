package com.vedovatto.setslip.e2e.support;

import com.vedovatto.setslip.e2e.config.TestConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class Device {

    private static final long ADB_TIMEOUT_SECONDS = 10;

    private Device() {}

    public static Optional<Float> animatorScale() {
        return adbShell("settings", "get", "global", "animator_duration_scale")
                .map(value -> value.equals("null") ? "1" : value)
                .flatMap(Device::parseScale);
    }

    public static void setAnimatorScale(float scale) {
        putSetting("global", "animator_duration_scale", Float.toString(scale));
    }

    public static void confirmImmersiveMode() {
        putSetting("secure", "immersive_mode_confirmations", "confirmed");
    }

    public static void hideErrorDialogs() {
        putSetting("global", "hide_error_dialogs", "1");
    }

    private static void putSetting(String namespace, String key, String value) {
        if (adbShell("settings", "put", namespace, key, value).isEmpty()) {
            System.err.println("Could not set " + namespace + " " + key + " on the device");
        }
    }

    private static Optional<Float> parseScale(String value) {
        try {
            return Optional.of(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> adbShell(String... arguments) {
        List<String> command = new ArrayList<>(List.of(adb()));
        TestConfig.deviceUdid().ifPresent(udid -> command.addAll(List.of("-s", udid)));
        command.add("shell");
        command.addAll(List.of(arguments));
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            if (!process.waitFor(ADB_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return Optional.empty();
            }
            if (process.exitValue() != 0) {
                return Optional.empty();
            }
            return Optional.of(
                    new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                            .trim());
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private static String adb() {
        String executable =
                System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")
                        ? "adb.exe"
                        : "adb";
        return Stream.of("ANDROID_HOME", "ANDROID_SDK_ROOT")
                .map(System::getenv)
                .filter(home -> home != null && !home.isBlank())
                .map(home -> Path.of(home, "platform-tools", executable))
                .filter(Files::isExecutable)
                .map(Path::toString)
                .findFirst()
                .orElse("adb");
    }
}
