package com.vedovatto.setslip.e2e.screens;

import com.vedovatto.setslip.e2e.support.Group;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;

public class PrinterScreen extends Screen {

    private static final String KEY_PREFIX = "key_";
    private static final By ENABLED_KEYS = enabledWithPrefix(KEY_PREFIX);
    private static final By SELECTED_KEYS = checkedWithPrefix(KEY_PREFIX);
    private static final By UNLOCKED_CONTROLS = enabledMatching("(key_.+|btn_print|btn_another)");
    private static final By PRINTER = tagged("printer");
    private static final By EMPTY_HINT = tagged("hint_empty");
    private static final By STATUS = tagged("status");
    private static final By SOUND_LIGHT = tagged("led");
    private static final By PRINT = tagged("btn_print");
    private static final By ANOTHER = tagged("btn_another");
    private static final By SETTINGS = tagged("btn_settings");
    private static final By FAVORITES = tagged("btn_favorites");
    private static final By FAVORITES_BADGE = tagged("badge_favorites");
    private static final By TIP = tagged("toast_tip");
    private static final By GOT_IT = tagged("btn_got_it");
    private static final By NO_EXERCISES_NOTICE = tagged("toast_blocked");
    private static final By REACTIVATE = tagged("btn_reactivate");

    private static By key(Group group) {
        return tagged(KEY_PREFIX + group.id());
    }

    public void waitUntilOpen() {
        shown(PRINTER);
        shown(PRINT);
    }

    public boolean isOpen() {
        return isPresent(PRINT);
    }

    public boolean isEmpty() {
        return isPresent(EMPTY_HINT);
    }

    public void select(Group group) {
        if (!isSelected(group)) {
            tap(key(group));
        }
    }

    public void deselect(Group group) {
        if (isSelected(group)) {
            tap(key(group));
        }
    }

    public void selectOnly(Collection<Group> groups) {
        List<Group> selected = selectedGroups();
        selected.stream().filter(group -> !groups.contains(group)).forEach(this::deselect);
        groups.stream().filter(group -> !selected.contains(group)).forEach(this::select);
    }

    public List<Group> selectedGroups() {
        return tags(SELECTED_KEYS).stream()
                .map(tag -> Group.named(tag.substring(KEY_PREFIX.length())))
                .toList();
    }

    public void pressPrint() {
        tap(PRINT);
    }

    public void pressAnother() {
        tap(ANOTHER);
    }

    public boolean canPrint() {
        return isEnabled(PRINT);
    }

    public boolean canPrintAnother() {
        return isEnabled(ANOTHER);
    }

    public boolean isLocked() {
        return count(UNLOCKED_CONTROLS) == 0;
    }

    public boolean keysEnabled() {
        return count(ENABLED_KEYS) == Group.values().length;
    }

    public void waitUntilSlipReady() {
        waitingFor("the slip to finish printing").until(driver -> canPrintAnother());
    }

    public String printLabel() {
        List<String> labels = textsInside(PRINT);
        return labels.isEmpty() ? "" : labels.get(labels.size() - 1);
    }

    public String announcement() {
        return description(STATUS);
    }

    public void tapSoundLight() {
        tap(SOUND_LIGHT);
    }

    public boolean tipShown() {
        return isPresent(TIP);
    }

    public void dismissTip() {
        tap(GOT_IT);
        waitUntilGone(TIP);
    }

    public boolean noExercisesNoticeShown() {
        return isPresent(NO_EXERCISES_NOTICE);
    }

    public void reactivate() {
        tap(REACTIVATE);
        waitUntilGone(NO_EXERCISES_NOTICE);
    }

    public Optional<Integer> favoritesBadge() {
        return textIfPresent(FAVORITES_BADGE).map(Integer::parseInt);
    }

    public void openSettings() {
        tap(SETTINGS);
    }

    public void openFavorites() {
        tap(FAVORITES);
    }

    private boolean isSelected(Group group) {
        return isChecked(key(group));
    }
}
