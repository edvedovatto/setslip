package com.vedovatto.setslip.e2e.steps;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.support.App;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AppSteps {

    private final PrinterScreen printer;

    public AppSteps(PrinterScreen printer) {
        this.printer = printer;
    }

    @When("I restart the app")
    public void restartApp() {
        App.restart();
        printer.waitUntilOpen();
    }

    @When("I go back")
    public void goBack() {
        App.pressBack();
    }

    @Then("the system share sheet opens")
    public void shareSheetOpens() {
        eventually(
                () ->
                        assertThat(App.foregroundActivity())
                                .as("activity in the foreground")
                                .contains("ChooserActivity"));
    }
}
