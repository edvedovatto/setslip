package com.vedovatto.setslip.e2e.support;

import java.util.Locale;

public enum Language {
    PORTUGUESE("pt"),
    ENGLISH("en");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Language named(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
