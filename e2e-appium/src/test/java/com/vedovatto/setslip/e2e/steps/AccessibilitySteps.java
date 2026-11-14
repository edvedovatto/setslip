package com.vedovatto.setslip.e2e.steps;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.Controls;
import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.screens.Slip;
import io.cucumber.java.en.Then;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;

public class AccessibilitySteps {

    private final Controls controls;
    private final PrinterScreen printer;
    private final Slip slip;

    public AccessibilitySteps(Controls controls, PrinterScreen printer, Slip slip) {
        this.controls = controls;
        this.printer = printer;
        this.slip = slip;
    }

    @Then("these controls are described to screen readers:")
    public void controlsAreDescribed(Map<String, String> descriptions) {
        eventually(
                () -> {
                    SoftAssertions soft = new SoftAssertions();
                    descriptions.forEach(
                            (control, description) ->
                                    soft.assertThat(controls.descriptionOf(control))
                                            .as(control)
                                            .isEqualTo(description));
                    soft.assertAll();
                });
    }

    @Then("every control on the panel has a touch target of at least {int}dp")
    public void panelControlsAreLargeEnough(int minimum) {
        eventually(
                () -> {
                    SoftAssertions soft = new SoftAssertions();
                    for (String control : controls.onThePanel()) {
                        Controls.Size size = controls.sizeOf(control);
                        soft.assertThat(size.widthDp())
                                .as(control + " width")
                                .isGreaterThanOrEqualTo(minimum);
                        soft.assertThat(size.heightDp())
                                .as(control + " height")
                                .isGreaterThanOrEqualTo(minimum);
                    }
                    soft.assertAll();
                });
    }

    @Then("exercise {int} is exposed as an unchecked checkbox")
    public void exposedAsUncheckedCheckbox(int position) {
        String exercise = slip.exerciseAt(position);
        eventually(
                () -> {
                    assertThat(slip.isCheckbox(exercise)).as("checkable").isTrue();
                    assertThat(slip.isChecked(exercise)).as("checked").isFalse();
                });
    }

    @Then("exercise {int} is exposed as a checked checkbox")
    public void exposedAsCheckedCheckbox(int position) {
        String exercise = slip.exerciseAt(position);
        eventually(
                () -> {
                    assertThat(slip.isCheckbox(exercise)).as("checkable").isTrue();
                    assertThat(slip.isChecked(exercise)).as("checked").isTrue();
                });
    }

    @Then("exercise {int} is exposed as disabled")
    public void exposedAsDisabled(int position) {
        String exercise = slip.exerciseAt(position);
        eventually(() -> assertThat(slip.isCrossedOut(exercise)).as("disabled").isTrue());
    }

    @Then("the printer announces {string}")
    public void printerAnnounces(String announcement) {
        eventually(() -> assertThat(printer.announcement()).isEqualTo(announcement));
    }
}
