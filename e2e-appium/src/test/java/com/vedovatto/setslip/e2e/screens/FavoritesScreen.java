package com.vedovatto.setslip.e2e.screens;

import com.vedovatto.setslip.e2e.support.Group;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.openqa.selenium.By;

public class FavoritesScreen extends Screen {

    private static final String SLIP_PREFIX = "fav_";
    private static final By SCREEN = tagged("screen_favorites");
    private static final By SLIPS = taggedWithPrefix(SLIP_PREFIX);
    private static final By NOTHING_HERE = tagged("favorites_empty");
    private static final By USE = tagged("btn_use");

    private static By previewOf(String slipTag) {
        return inside(slipTag, "slip_counter");
    }

    private static Set<Group> groupsOf(String slipTag) {
        String groups = slipTag.substring(SLIP_PREFIX.length(), slipTag.lastIndexOf('_'));
        return Arrays.stream(groups.split("_"))
                .map(Group::named)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void waitUntilOpen() {
        shown(SCREEN);
    }

    public boolean nothingListed() {
        return isPresent(NOTHING_HERE);
    }

    public List<Set<Group>> slips() {
        return tags(SLIPS).stream().map(FavoritesScreen::groupsOf).toList();
    }

    public void open(Collection<Group> groups) {
        String slipTag =
                waitingFor("a favorite slip for " + groups)
                        .until(driver -> tagOfSlipFor(groups).orElse(null));
        if (!isPresent(previewOf(slipTag))) {
            tap(tagged(slipTag));
        }
        shown(previewOf(slipTag));
    }

    public void useOpenSlip() {
        tap(USE);
        waitUntilGone(SCREEN);
    }

    public void filterBy(Group group) {
        By filter = tagged("filter_" + group.id());
        if (!isChecked(filter)) {
            tap(filter);
        }
        waitingFor("the " + group.id() + " filter to be on").until(driver -> isChecked(filter));
    }

    private Optional<String> tagOfSlipFor(Collection<Group> groups) {
        Set<Group> wanted = Set.copyOf(groups);
        return tags(SLIPS).stream().filter(tag -> groupsOf(tag).equals(wanted)).findFirst();
    }
}
