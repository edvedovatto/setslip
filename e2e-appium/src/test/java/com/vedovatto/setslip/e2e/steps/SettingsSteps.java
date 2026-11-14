package com.vedovatto.setslip.e2e.steps;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.screens.SettingsScreen;
import com.vedovatto.setslip.e2e.support.Language;
import com.vedovatto.setslip.e2e.support.Memory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SettingsSteps {

    private final PrinterScreen printer;
    private final SettingsScreen settings;
    private final Memory memory;

    public SettingsSteps(PrinterScreen printer, SettingsScreen settings, Memory memory) {
        this.printer = printer;
        this.settings = settings;
        this.memory = memory;
    }

    @Given("the app is in {language}")
    public void appIsIn(Language language) {
        openSettings();
        chooseLanguage(language);
        closeSettings();
    }

    @When("I open the settings")
    public void openSettings() {
        printer.openSettings();
        settings.waitUntilOpen();
    }

    @When("I close the settings")
    public void closeSettings() {
        settings.pressBack();
        printer.waitUntilOpen();
    }

    @When("I choose {language} as the language")
    public void chooseLanguage(Language language) {
        settings.chooseLanguage(language);
    }

    @When("I turn off vibration")
    public void turnOffVibration() {
        settings.turnVibrationOff();
    }

    @When("I turn the sound back on")
    public void turnSoundOn() {
        settings.turnSoundOn();
    }

    @When("I restore it")
    public void restoreExercise() {
        settings.restore(memory.exercise());
    }

    @When("I restore all crossed-out exercises")
    public void restoreAll() {
        settings.restoreAll();
    }

    @Then("the language is {language}")
    public void languageIs(Language language) {
        eventually(
                () ->
                        assertThat(settings.languageChosen(language))
                                .as(language + " chosen")
                                .isTrue());
    }

    @Then("sound is on")
    public void soundIsOn() {
        eventually(() -> assertThat(settings.soundOn()).as("sound switch").isTrue());
    }

    @Then("sound is off")
    public void soundIsOff() {
        eventually(() -> assertThat(settings.soundOn()).as("sound switch").isFalse());
    }

    @Then("vibration is on")
    public void vibrationIsOn() {
        eventually(() -> assertThat(settings.vibrationOn()).as("vibration switch").isTrue());
    }

    @Then("vibration is off")
    public void vibrationIsOff() {
        eventually(() -> assertThat(settings.vibrationOn()).as("vibration switch").isFalse());
    }

    @Then("the crossed-out exercise is listed in the settings")
    public void crossedOutExerciseIsListed() {
        eventually(() -> assertThat(settings.crossedOut()).containsExactly(memory.exercise()));
    }

    @Then("the settings list {int} crossed-out exercises")
    public void settingsListCrossedOut(int count) {
        eventually(() -> assertThat(settings.crossedOut()).hasSize(count));
    }

    @Then("the settings list no crossed-out exercises")
    public void nothingCrossedOut() {
        eventually(
                () -> {
                    assertThat(settings.nothingCrossedOut()).as("nothing crossed out").isTrue();
                    assertThat(settings.crossedOut()).isEmpty();
                });
    }
}
