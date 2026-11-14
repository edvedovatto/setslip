package com.vedovatto.setslip.e2e.steps;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static com.vedovatto.setslip.e2e.support.Eventually.staysTrue;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.screens.Slip;
import com.vedovatto.setslip.e2e.support.App;
import com.vedovatto.setslip.e2e.support.Group;
import com.vedovatto.setslip.e2e.support.Memory;
import com.vedovatto.setslip.e2e.support.SlipPrinting;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.List;

public class PrinterSteps {

    private static final Duration TIP_WATCH = Duration.ofSeconds(2);

    private final PrinterScreen printer;
    private final Slip slip;
    private final SlipPrinting printing;
    private final Memory memory;

    public PrinterSteps(PrinterScreen printer, Slip slip, SlipPrinting printing, Memory memory) {
        this.printer = printer;
        this.slip = slip;
        this.printing = printing;
        this.memory = memory;
    }

    @Given("a printed slip for {groups}")
    @When("I print a slip for {groups}")
    public void printSlipFor(List<Group> groups) {
        printing.print(groups);
    }

    @When("I ask for another slip")
    public void askForAnotherSlip() {
        memory.rememberPreviousSlip(slip.exercises());
        printing.printAnother();
    }

    @When("I select {groups}")
    public void select(List<Group> groups) {
        groups.forEach(printer::select);
    }

    @When("I deselect {groups}")
    public void deselect(List<Group> groups) {
        groups.forEach(printer::deselect);
    }

    @When("I press print")
    public void pressPrint() {
        printer.pressPrint();
    }

    @When("I press another")
    public void pressAnother() {
        printer.pressAnother();
    }

    @When("the slip is ready")
    public void slipIsReady() {
        printer.waitUntilSlipReady();
    }

    @When("I dismiss the tip")
    public void dismissTip() {
        printing.dismissTip();
    }

    @When("I reactivate the exercises")
    public void reactivateExercises() {
        printer.reactivate();
    }

    @When("I mute the printer with the sound light")
    public void muteWithSoundLight() {
        printer.muteWithSoundLight();
    }

    @Then("the printer is empty")
    public void printerIsEmpty() {
        eventually(() -> assertThat(printer.isEmpty()).as("empty printer hint").isTrue());
    }

    @Then("I can print")
    public void canPrint() {
        eventually(() -> assertThat(printer.canPrint()).as("print enabled").isTrue());
    }

    @Then("I cannot print")
    public void cannotPrint() {
        eventually(() -> assertThat(printer.canPrint()).as("print enabled").isFalse());
    }

    @Then("I cannot ask for another slip")
    public void cannotAskForAnother() {
        eventually(() -> assertThat(printer.canPrintAnother()).as("another enabled").isFalse());
    }

    @Then("the selected groups are {groups}")
    public void selectedGroupsAre(List<Group> groups) {
        eventually(
                () ->
                        assertThat(printer.selectedGroups())
                                .containsExactlyInAnyOrderElementsOf(groups));
    }

    @Then("the panel is locked")
    public void panelIsLocked() {
        eventually(() -> assertThat(printer.isLocked()).as("panel locked").isTrue());
    }

    @Then("the panel is unlocked")
    public void panelIsUnlocked() {
        eventually(
                () -> {
                    assertThat(printer.keysEnabled()).as("group keys enabled").isTrue();
                    assertThat(printer.canPrintAnother()).as("another enabled").isTrue();
                });
    }

    @Then("the tip is shown")
    public void tipIsShown() {
        eventually(() -> assertThat(printer.tipShown()).as("first slip tip").isTrue());
    }

    @Then("the tip is gone")
    @Then("the tip does not show up")
    public void tipDoesNotShowUp() {
        staysTrue(TIP_WATCH, () -> assertThat(printer.tipShown()).as("first slip tip").isFalse());
    }

    @Then("I am told there are no exercises left")
    public void toldNoExercisesLeft() {
        eventually(
                () ->
                        assertThat(printer.noExercisesNoticeShown())
                                .as("no exercises notice")
                                .isTrue());
    }

    @Then("the print button reads {string}")
    public void printButtonReads(String label) {
        eventually(() -> assertThat(printer.printLabel()).isEqualTo(label));
    }

    @Then("the favorites badge shows {int}")
    public void favoritesBadgeShows(int count) {
        eventually(() -> assertThat(printer.favoritesBadge()).hasValue(count));
    }

    @Then("there is no favorites badge")
    public void noFavoritesBadge() {
        eventually(() -> assertThat(printer.favoritesBadge()).isEmpty());
    }

    @Then("I am back at the printer")
    public void backAtThePrinter() {
        eventually(
                () -> {
                    assertThat(App.inForeground()).as("app in the foreground").isTrue();
                    assertThat(printer.isOpen()).as("printer panel visible").isTrue();
                });
    }
}
