@persistence @regression
Feature: What the app remembers after it is closed

  @smoke
  Scenario: Favorites are kept
    Given a favorite slip for chest
    When I restart the app
    Then the favorites badge shows 1
    When I open the favorites
    Then the favorites list only the slip for chest

  Scenario: Crossed-out exercises are kept
    Given a printed slip for chest
    And exercise 2 is crossed out
    When I restart the app
    And I open the settings
    Then the crossed-out exercise is listed in the settings

  Scenario: The language is kept
    Given the app is in English
    When I restart the app
    Then the print button reads "PRINT"

  Scenario: Sound and vibration choices are kept
    When I mute the printer with the sound light
    And I open the settings
    And I turn off vibration
    And I restart the app
    And I open the settings
    Then sound is off
    And vibration is off

  Scenario: Variations carry on where they stopped
    Given a printed slip for chest
    When I restart the app
    And I print a slip for chest
    Then the slip is variation B

  Scenario: The first slip tip shows only once
    Given a printed slip for chest
    When I restart the app
    And I print a slip for back
    Then the tip does not show up

  Scenario: The printed slip is not kept
    Given a printed slip for chest
    When I restart the app
    Then the printer is empty
    And I cannot ask for another slip
