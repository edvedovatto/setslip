package com.vedovatto.setslip.e2e.support;

import java.util.List;

public class Memory {

    private List<String> previousSlip = List.of();
    private List<String> favoriteSlip = List.of();
    private String exercise;
    private boolean tipDismissed;

    public List<String> previousSlip() {
        return previousSlip;
    }

    public void rememberPreviousSlip(List<String> exercises) {
        previousSlip = List.copyOf(exercises);
    }

    public List<String> favoriteSlip() {
        return favoriteSlip;
    }

    public void rememberFavoriteSlip(List<String> exercises) {
        favoriteSlip = List.copyOf(exercises);
    }

    public String exercise() {
        if (exercise == null) {
            throw new IllegalStateException("No exercise was picked earlier in this scenario");
        }
        return exercise;
    }

    public void rememberExercise(String id) {
        exercise = id;
    }

    public boolean tipDismissed() {
        return tipDismissed;
    }

    public void rememberTipDismissed() {
        tipDismissed = true;
    }
}
