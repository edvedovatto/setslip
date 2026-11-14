package com.vedovatto.setslip.e2e.steps;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.FavoritesScreen;
import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.screens.Slip;
import com.vedovatto.setslip.e2e.support.Group;
import com.vedovatto.setslip.e2e.support.Memory;
import com.vedovatto.setslip.e2e.support.SlipPrinting;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Set;

public class FavoritesSteps {

    private final PrinterScreen printer;
    private final Slip slip;
    private final FavoritesScreen favorites;
    private final SlipPrinting printing;
    private final Memory memory;

    public FavoritesSteps(
            PrinterScreen printer,
            Slip slip,
            FavoritesScreen favorites,
            SlipPrinting printing,
            Memory memory) {
        this.printer = printer;
        this.slip = slip;
        this.favorites = favorites;
        this.printing = printing;
        this.memory = memory;
    }

    @Given("a favorite slip for {groups}")
    public void favoriteSlipFor(List<Group> groups) {
        printing.print(groups);
        favoriteTheSlip();
    }

    @When("I favorite the slip")
    public void favoriteTheSlip() {
        memory.rememberFavoriteSlip(slip.exercises());
        slip.favorite();
    }

    @When("I unfavorite the slip")
    public void unfavoriteTheSlip() {
        slip.unfavorite();
    }

    @When("I open the favorites")
    public void openFavorites() {
        printer.openFavorites();
        favorites.waitUntilOpen();
    }

    @When("I use the favorite slip for {groups}")
    public void useFavoriteSlipFor(List<Group> groups) {
        favorites.open(groups);
        favorites.useOpenSlip();
        printer.waitUntilOpen();
        printer.waitUntilSlipReady();
    }

    @When("I filter the favorites by {group}")
    public void filterBy(Group group) {
        favorites.filterBy(group);
    }

    @Then("the slip is marked as favorite")
    public void slipIsFavorite() {
        eventually(() -> assertThat(slip.isFavorite()).as("favorite switch").isTrue());
    }

    @Then("the slip is not marked as favorite")
    public void slipIsNotFavorite() {
        eventually(() -> assertThat(slip.isFavorite()).as("favorite switch").isFalse());
    }

    @Then("no favorite slips are listed")
    public void noFavoritesListed() {
        eventually(
                () -> {
                    assertThat(favorites.nothingListed()).as("empty favorites message").isTrue();
                    assertThat(favorites.slips()).isEmpty();
                });
    }

    @Then("the favorites list only the slip for {groups}")
    public void favoritesListOnly(List<Group> groups) {
        eventually(() -> assertThat(favorites.slips()).containsExactly(Set.copyOf(groups)));
    }

    @Then("the favorites list {int} slips")
    public void favoritesList(int count) {
        eventually(() -> assertThat(favorites.slips()).hasSize(count));
    }

    @Then("the slip has the same exercises as the favorite")
    public void sameExercisesAsFavorite() {
        eventually(() -> assertThat(slip.exercises()).isEqualTo(memory.favoriteSlip()));
    }
}
