package com.vedovatto.setslip.e2e.steps;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.HowToScreen;
import com.vedovatto.setslip.e2e.screens.Slip;
import com.vedovatto.setslip.e2e.support.Memory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Locale;

public class HowToSteps {

    private final Slip slip;
    private final HowToScreen howTo;
    private final Memory memory;
    private String exerciseName;

    public HowToSteps(Slip slip, HowToScreen howTo, Memory memory) {
        this.slip = slip;
        this.howTo = howTo;
        this.memory = memory;
    }

    @Given("the how-to is open")
    @When("I open the how-to")
    public void openHowTo() {
        memory.rememberPreviousSlip(slip.exercises());
        slip.openHowTo();
        howTo.waitUntilIndexOpen();
    }

    @When("I open exercise {int} from the index")
    public void openExerciseFromIndex(int position) {
        String exercise = howTo.exerciseAt(position);
        exerciseName = howTo.nameOf(exercise);
        howTo.open(exercise);
    }

    @When("I press the back button")
    public void pressBackButton() {
        howTo.pressBack();
    }

    @Then("the index lists the exercises of the slip in the same order")
    public void indexListsSlipExercises() {
        eventually(
                () ->
                        assertThat(howTo.exercises())
                                .isNotEmpty()
                                .containsExactlyElementsOf(memory.previousSlip()));
    }

    @Then("I see the name, the picture and the steps of that exercise")
    public void seeExerciseDetail() {
        assertThat(exerciseName).as("exercise name in the index").isNotBlank();
        eventually(
                () -> {
                    assertThat(howTo.title()).isEqualTo(exerciseName.toUpperCase(Locale.ROOT));
                    assertThat(howTo.pictureShown()).as("exercise picture").isTrue();
                });
        howTo.scrollToSteps();
        eventually(
                () ->
                        assertThat(howTo.steps())
                                .as("how-to steps")
                                .hasSizeGreaterThanOrEqualTo(2)
                                .noneMatch(String::isBlank));
    }

    @Then("I see the index")
    public void seeIndex() {
        eventually(
                () -> {
                    assertThat(howTo.indexOpen()).as("index open").isTrue();
                    assertThat(howTo.detailOpen()).as("detail open").isFalse();
                });
    }
}
