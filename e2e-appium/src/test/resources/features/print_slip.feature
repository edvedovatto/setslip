@regression
Feature: Print a workout slip

  Scenario: Nothing is printed until I ask for it
    Then the printer is empty
    And I cannot print
    And I cannot ask for another slip

  @smoke
  Scenario: Print the first slip
    When I print a slip for chest
    Then the slip is variation A
    And the slip lists 6 exercises
    And 0 of 6 exercises are done

  Scenario Outline: The number of exercises depends on the groups
    When I print a slip for <groups>
    Then the slip lists <exercises> exercises

    Examples:
      | groups                 | exercises |
      | back                   | 6         |
      | biceps, triceps        | 6         |
      | chest, biceps          | 5         |
      | chest, back, abs       | 8         |
      | shoulders, biceps, abs | 9         |

  Scenario: A fourth group is not accepted
    Given the app is in English
    When I select chest, back, shoulders
    And I select biceps
    Then the printer announces "up to 3 groups"
    And the selected groups are chest, back, shoulders

  Scenario: The same groups cannot be printed twice in a row
    Given a printed slip for chest, biceps
    Then I cannot print
    When I deselect chest
    Then I can print
    When I select chest
    Then I cannot print

  Scenario: Another slip brings different exercises
    Given a printed slip for chest
    When I ask for another slip
    Then the slip is variation B
    And the slip lists 6 exercises
    And the exercises differ from the previous slip

  Scenario: Variations go round and start over
    Given a printed slip for chest
    When I ask for another slip
    Then the slip is variation B
    When I ask for another slip
    Then the slip is variation C
    When I ask for another slip
    Then the slip is variation A

  @animations
  Scenario: The panel is locked while the slip prints
    When I select chest
    And I press print
    Then the panel is locked
    When the slip is ready
    Then the panel is unlocked

  Scenario: The first slip comes with a tip
    When I select chest
    And I press print
    And the slip is ready
    Then the tip is shown
    When I dismiss the tip
    Then the tip is gone
