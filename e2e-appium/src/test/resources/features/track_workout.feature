@regression
Feature: Track the workout on the slip

  Background:
    Given a printed slip for chest

  @smoke
  Scenario: Check an exercise as done
    When I check exercise 1
    Then exercise 1 is checked
    And 1 of 6 exercises are done

  Scenario: Uncheck an exercise
    When I check exercise 1
    And I uncheck exercise 1
    Then exercise 1 is not checked
    And 0 of 6 exercises are done

  Scenario: A new slip starts with nothing checked
    When I check exercise 1
    And I ask for another slip
    Then 0 of 6 exercises are done

  Scenario: Reading the how-to does not lose my progress
    When I check exercise 1
    And I open the how-to
    And I go back
    Then exercise 1 is checked
    And 1 of 6 exercises are done

  Scenario: Cross out an exercise I do not want
    When I cross out exercise 2
    Then I am offered to undo it
    And exercise 2 is shown crossed out
    And 0 of 5 exercises are done

  Scenario: Undo a crossed-out exercise
    When I cross out exercise 2
    And I undo it
    Then exercise 2 is not crossed out
    And 0 of 6 exercises are done

  Scenario: A crossed-out exercise no longer counts as done
    When I check exercise 1
    And I cross out exercise 1
    Then 0 of 5 exercises are done

  Scenario: A crossed-out exercise does not come back
    When I cross out exercise 3
    And I ask for another slip
    Then the crossed-out exercise is not on the slip

  Scenario: Running out of exercises for a group
    When I cross out every exercise on the slip
    Then the slip has nothing left to do
    And the slip counter reads "FICHA A · TUDO RISCADO"
    When I ask for another slip
    Then the slip lists 2 exercises
    When I cross out every exercise on the slip
    And I press another
    Then I am told there are no exercises left
    When I reactivate the exercises
    And I ask for another slip
    Then the slip lists 6 exercises

  Scenario: Share the slip
    When I share the slip
    Then the system share sheet opens
    When I go back
    Then I am back at the printer
