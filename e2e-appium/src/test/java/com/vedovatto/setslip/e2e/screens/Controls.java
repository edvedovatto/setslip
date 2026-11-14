package com.vedovatto.setslip.e2e.screens;

import com.vedovatto.setslip.e2e.support.Group;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;

public class Controls extends Screen {

    private static final int DP_BASELINE_DENSITY = 160;

    private static final Map<String, String> BUTTONS =
            Map.of(
                    "settings", "btn_settings",
                    "favorites", "btn_favorites",
                    "another slip", "btn_another",
                    "print", "btn_print",
                    "sound light", "led",
                    "how to", "btn_how_to",
                    "favorite slip", "btn_favorite",
                    "share slip", "btn_share");

    public record Size(int widthDp, int heightDp) {}

    public List<String> onThePanel() {
        Stream<String> keys = Arrays.stream(Group.values()).map(group -> group.id() + " key");
        return Stream.concat(keys, Stream.of("settings", "favorites", "another slip", "print"))
                .toList();
    }

    public String descriptionOf(String control) {
        return description(locate(control));
    }

    public Size sizeOf(String control) {
        Rectangle bounds = bounds(locate(control));
        Number density = (Number) driver().executeScript("mobile: getDisplayDensity");
        int dpi = density.intValue();
        return new Size(toDp(bounds.getWidth(), dpi), toDp(bounds.getHeight(), dpi));
    }

    private static int toDp(int pixels, int dpi) {
        return Math.round(pixels * (float) DP_BASELINE_DENSITY / dpi);
    }

    private static By locate(String control) {
        if (control.endsWith(" key")) {
            return tagged("key_" + control.substring(0, control.length() - " key".length()));
        }
        String tag = BUTTONS.get(control);
        if (tag == null) {
            throw new IllegalArgumentException("Unknown control: " + control);
        }
        return tagged(tag);
    }
}
