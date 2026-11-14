package com.vedovatto.setslip.e2e.support;

import io.cucumber.java.ParameterType;
import java.util.List;

public class ParameterTypes {

    @ParameterType("[a-z]+(?:, [a-z]+)*")
    public List<Group> groups(String names) {
        return Group.listed(names);
    }

    @ParameterType("[a-z]+")
    public Group group(String name) {
        return Group.named(name);
    }

    @ParameterType("Portuguese|English")
    public Language language(String name) {
        return Language.named(name);
    }

    @ParameterType("[A-C]")
    public String variation(String letter) {
        return letter;
    }
}
