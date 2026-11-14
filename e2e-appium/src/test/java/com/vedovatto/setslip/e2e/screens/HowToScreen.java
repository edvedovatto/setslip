package com.vedovatto.setslip.e2e.screens;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.openqa.selenium.By;

public class HowToScreen extends Screen {

    private static final String ENTRY_PREFIX = "index_";
    private static final By INDEX = tagged("screen_index");
    private static final By ENTRIES = taggedWithPrefix(ENTRY_PREFIX);
    private static final By DETAIL = tagged("screen_detail");
    private static final By TITLE = tagged("detail_title");
    private static final By PICTURE = tagged("detail_image");
    private static final String STEPS_TAG = "detail_steps";
    private static final By STEPS = tagged(STEPS_TAG);

    private static final Predicate<String> NUMBERING = Pattern.compile("\\d+").asMatchPredicate();

    public void waitUntilIndexOpen() {
        shown(INDEX);
    }

    public boolean indexOpen() {
        return isPresent(INDEX);
    }

    public boolean detailOpen() {
        return isPresent(DETAIL);
    }

    public List<String> exercises() {
        return tags(ENTRIES).stream().map(tag -> tag.substring(ENTRY_PREFIX.length())).toList();
    }

    public String exerciseAt(int position) {
        return waitingFor("exercise " + position + " in the index")
                .until(
                        driver -> {
                            List<String> exercises = exercises();
                            return exercises.size() < position ? null : exercises.get(position - 1);
                        });
    }

    public String nameOf(String exercise) {
        return textsInside(tagged(ENTRY_PREFIX + exercise)).stream()
                .filter(NUMBERING.negate())
                .findFirst()
                .orElse("");
    }

    public void open(String exercise) {
        tap(tagged(ENTRY_PREFIX + exercise));
        shown(DETAIL);
    }

    public String title() {
        return text(TITLE);
    }

    public boolean pictureShown() {
        return isPresent(PICTURE);
    }

    public void scrollToSteps() {
        scrollTo(STEPS_TAG);
    }

    public List<String> steps() {
        return textsInside(STEPS).stream().filter(NUMBERING.negate()).toList();
    }

    public void pressBack() {
        tap(BACK);
    }
}
