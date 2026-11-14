package com.vedovatto.setslip.e2e.steps;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.Slip;
import com.vedovatto.setslip.e2e.support.Memory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SlipSteps {

    private final Slip slip;
    private final Memory memory;

    public SlipSteps(Slip slip, Memory memory) {
        this.slip = slip;
        this.memory = memory;
    }

    @When("I check exercise {int}")
    public void checkExercise(int position) {
        slip.check(slip.exerciseAt(position));
    }

    @When("I uncheck exercise {int}")
    public void uncheckExercise(int position) {
        slip.uncheck(slip.exerciseAt(position));
    }

    @Given("exercise {int} is crossed out")
    @When("I cross out exercise {int}")
    public void crossOutExercise(int position) {
        String exercise = slip.exerciseAt(position);
        slip.crossOut(exercise);
        memory.rememberExercise(exercise);
    }

    @When("I cross out exercise {int} and am offered to undo it")
    public void crossOutAndSeeUndoOffer(int position) {
        slip.crossOutAndWaitForUndoOffer(slip.exerciseAt(position));
    }

    @When("I cross out exercise {int} and undo it right away")
    public void crossOutAndUndo(int position) {
        slip.crossOutAndUndo(slip.exerciseAt(position));
    }

    @When("I cross out every exercise on the slip")
    public void crossOutEveryExercise() {
        slip.exercises().forEach(slip::crossOut);
    }

    @When("I share the slip")
    public void shareSlip() {
        slip.share();
    }

    @Then("the slip is variation {variation}")
    public void slipIsVariation(String letter) {
        eventually(() -> assertThat(slip.variation()).contains(letter));
    }

    @Then("the slip lists {int} exercise(s)")
    public void slipListsExercises(int count) {
        eventually(() -> assertThat(slip.exercises()).hasSize(count));
    }

    @Then("{int} of {int} exercises are done")
    public void exercisesAreDone(int done, int total) {
        eventually(() -> assertThat(slip.progress()).contains(new Slip.Progress(done, total)));
    }

    @Then("the slip has nothing left to do")
    public void nothingLeftToDo() {
        eventually(
                () -> assertThat(slip.allCrossedOut()).as("every exercise crossed out").isTrue());
    }

    @Then("the slip counter reads {string}")
    public void counterReads(String text) {
        eventually(() -> assertThat(slip.counter()).isEqualTo(text));
    }

    @Then("exercise {int} is checked")
    public void exerciseIsChecked(int position) {
        String exercise = slip.exerciseAt(position);
        eventually(() -> assertThat(slip.isChecked(exercise)).as("checked").isTrue());
    }

    @Then("exercise {int} is not checked")
    public void exerciseIsNotChecked(int position) {
        String exercise = slip.exerciseAt(position);
        eventually(() -> assertThat(slip.isChecked(exercise)).as("checked").isFalse());
    }

    @Then("exercise {int} is shown crossed out")
    public void exerciseIsCrossedOut(int position) {
        String exercise = slip.exerciseAt(position);
        eventually(() -> assertThat(slip.isCrossedOut(exercise)).as("crossed out").isTrue());
    }

    @Then("exercise {int} is not crossed out")
    public void exerciseIsNotCrossedOut(int position) {
        String exercise = slip.exerciseAt(position);
        eventually(() -> assertThat(slip.isCrossedOut(exercise)).as("crossed out").isFalse());
    }

    @Then("the exercises differ from the previous slip")
    public void exercisesDifferFromPreviousSlip() {
        eventually(
                () ->
                        assertThat(slip.exercises())
                                .isNotEmpty()
                                .isNotEqualTo(memory.previousSlip()));
    }

    @Then("the crossed-out exercise is not on the slip")
    public void crossedOutExerciseIsNotOnTheSlip() {
        eventually(
                () -> assertThat(slip.exercises()).isNotEmpty().doesNotContain(memory.exercise()));
    }
}
