package com.vedovatto.setslip.e2e.screens;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;

public class Slip extends Screen {

    private static final String CURRENT_TAG = "paper_current";
    private static final String ROW_PREFIX = "row_";
    private static final By CURRENT = tagged(CURRENT_TAG);
    private static final By ROWS = taggedWithPrefix(ROW_PREFIX);
    private static final By ACTIVE_ROWS = enabledWithPrefix(ROW_PREFIX);
    private static final By COUNTER = inside(CURRENT_TAG, "slip_counter");
    private static final By HOW_TO = tagged("btn_how_to");
    private static final By FAVORITE = tagged("btn_favorite");
    private static final By SHARE = tagged("btn_share");
    private static final By CROSSED_OUT_NOTICE = tagged("toast_struck");
    private static final By UNDO = tagged("btn_undo");

    private static final Pattern VARIATION = Pattern.compile("^\\S+ ([A-C]) ");
    private static final Pattern DONE_OF_TOTAL = Pattern.compile("(\\d+)\\D+(\\d+)");

    public record Progress(int done, int total) {}

    public record Printed(String counter, List<String> exercises) {}

    private static By row(String exercise) {
        return inside(CURRENT_TAG, ROW_PREFIX + exercise);
    }

    public Printed printed() {
        return new Printed(textIfPresent(COUNTER).orElse(""), exercises());
    }

    public List<String> exercises() {
        return tagsWithin(CURRENT, ROWS).stream()
                .map(tag -> tag.substring(ROW_PREFIX.length()))
                .toList();
    }

    public String exerciseAt(int position) {
        return waitingFor("exercise " + position + " on the slip")
                .until(
                        driver -> {
                            List<String> exercises = exercises();
                            return exercises.size() < position ? null : exercises.get(position - 1);
                        });
    }

    public String counter() {
        return text(COUNTER);
    }

    public Optional<String> variation() {
        Matcher matcher = VARIATION.matcher(counter());
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    public Optional<Progress> progress() {
        Matcher matcher = DONE_OF_TOTAL.matcher(counter());
        if (!matcher.find()) {
            return Optional.empty();
        }
        int done = Integer.parseInt(matcher.group(1));
        int total = Integer.parseInt(matcher.group(2));
        return Optional.of(new Progress(done, total));
    }

    public boolean allCrossedOut() {
        return countWithin(CURRENT, ROWS) > 0 && countWithin(CURRENT, ACTIVE_ROWS) == 0;
    }

    public void check(String exercise) {
        setChecked(exercise, true);
    }

    public void uncheck(String exercise) {
        setChecked(exercise, false);
    }

    public void crossOut(String exercise) {
        longPress(row(exercise));
        waitingFor(exercise + " to be crossed out").until(driver -> isCrossedOut(exercise));
    }

    public void crossOutAndUndo(String exercise) {
        longPress(row(exercise));
        undo();
    }

    public boolean isChecked(String exercise) {
        return isChecked(row(exercise));
    }

    public boolean isCheckbox(String exercise) {
        return Boolean.parseBoolean(attribute(row(exercise), "checkable"));
    }

    public boolean isCrossedOut(String exercise) {
        return !isEnabled(row(exercise));
    }

    public boolean undoOffered() {
        return isPresent(CROSSED_OUT_NOTICE);
    }

    public void undo() {
        tap(UNDO);
        waitUntilGone(CROSSED_OUT_NOTICE);
    }

    public void openHowTo() {
        tap(HOW_TO);
    }

    public void favorite() {
        setFavorite(true);
    }

    public void unfavorite() {
        setFavorite(false);
    }

    public boolean isFavorite() {
        return isChecked(FAVORITE);
    }

    public void share() {
        tap(SHARE);
    }

    private void setChecked(String exercise, boolean checked) {
        if (isChecked(exercise) != checked) {
            tap(row(exercise));
        }
        waitingFor(exercise + (checked ? " to be checked" : " to be unchecked"))
                .until(driver -> isChecked(exercise) == checked);
    }

    private void setFavorite(boolean favorite) {
        if (isFavorite() != favorite) {
            tap(FAVORITE);
        }
        waitingFor("the slip to be " + (favorite ? "a favorite" : "out of the favorites"))
                .until(driver -> isFavorite() == favorite);
    }
}
