package com.vedovatto.setslip.e2e.support;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum Group {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    ABS;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Group named(String name) {
        return valueOf(name.trim().toUpperCase(Locale.ROOT));
    }

    public static List<Group> listed(String names) {
        return Arrays.stream(names.split(",")).map(Group::named).toList();
    }
}
