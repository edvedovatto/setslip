@regression
Feature: How to do each exercise

  Background:
    Given a printed slip for chest
    And the how-to is open

  Scenario: The index lists the exercises of the slip
    Then the index lists the exercises of the slip in the same order

  @smoke
  Scenario: An exercise shows its name, picture and steps
    When I open exercise 1 from the index
    Then I see the name, the picture and the steps of that exercise

  Scenario: The system back leaves one level at a time
    When I open exercise 1 from the index
    And I go back
    Then I see the index
    When I go back
    Then I am back at the printer

  Scenario: The back button leaves one level at a time
    When I open exercise 1 from the index
    And I press the back button
    Then I see the index
    When I press the back button
    Then I am back at the printer
