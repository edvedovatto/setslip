package com.vedovatto.setslip.e2e.screens;

import com.vedovatto.setslip.e2e.support.Language;
import java.util.List;
import org.openqa.selenium.By;

public class SettingsScreen extends Screen {

    private static final String CROSSED_OUT_PREFIX = "struck_";
    private static final String NOTHING_CROSSED_OUT_TAG = "struck_empty";
    private static final By SCREEN = tagged("screen_settings");
    private static final By SOUND = tagged("toggle_sound");
    private static final By VIBRATION = tagged("toggle_vibration");
    private static final By CROSSED_OUT = taggedWithPrefix(CROSSED_OUT_PREFIX);
    private static final By NOTHING_CROSSED_OUT = tagged(NOTHING_CROSSED_OUT_TAG);
    private static final By REACTIVATE_ALL = tagged("btn_reactivate_all");

    private static By option(Language language) {
        return tagged("btn_language_" + language.code());
    }

    public void waitUntilOpen() {
        shown(SCREEN);
    }

    public void chooseLanguage(Language language) {
        tap(option(language));
        waitingFor("the language to change to " + language.code())
                .until(driver -> languageChosen(language));
    }

    public boolean languageChosen(Language language) {
        return isChecked(option(language));
    }

    public boolean soundOn() {
        return isChecked(SOUND);
    }

    public void turnSoundOn() {
        if (!soundOn()) {
            tap(SOUND);
        }
        waitingFor("sound to be on").until(driver -> soundOn());
    }

    public boolean vibrationOn() {
        return isChecked(VIBRATION);
    }

    public void turnVibrationOff() {
        if (vibrationOn()) {
            tap(VIBRATION);
        }
        waitingFor("vibration to be off").until(driver -> !vibrationOn());
    }

    public List<String> crossedOut() {
        return tags(CROSSED_OUT).stream()
                .filter(tag -> !tag.equals(NOTHING_CROSSED_OUT_TAG))
                .map(tag -> tag.substring(CROSSED_OUT_PREFIX.length()))
                .toList();
    }

    public boolean nothingCrossedOut() {
        return isPresent(NOTHING_CROSSED_OUT);
    }

    public void restore(String exercise) {
        tap(tagged(CROSSED_OUT_PREFIX + exercise));
    }

    public void restoreAll() {
        tap(REACTIVATE_ALL);
    }

    public void pressBack() {
        tap(BACK);
        waitUntilGone(SCREEN);
    }
}
