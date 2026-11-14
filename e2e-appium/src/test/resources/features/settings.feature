@regression
Feature: Settings

  Scenario: The app starts in Portuguese with sound and vibration on
    Then the print button reads "IMPRIMIR"
    When I open the settings
    Then the language is Portuguese
    And sound is on
    And vibration is on

  @smoke
  Scenario: Switch to English
    Given a printed slip for chest
    When I open the settings
    And I choose English as the language
    And I close the settings
    Then the print button reads "PRINT"
    And the slip counter reads "SLIP A · 0 OF 6 DONE"

  Scenario: Mute the printer from its sound light
    When I mute the printer with the sound light
    And I open the settings
    Then sound is off
    When I turn the sound back on
    Then sound is on

  Scenario: Turn off vibration
    When I open the settings
    And I turn off vibration
    Then vibration is off

  Scenario: Restore a crossed-out exercise
    Given a printed slip for chest
    And exercise 2 is crossed out
    When I open the settings
    Then the crossed-out exercise is listed in the settings
    When I restore it
    Then the settings list no crossed-out exercises
    When I close the settings
    Then exercise 2 is not crossed out

  Scenario: Restore every crossed-out exercise at once
    Given a printed slip for chest
    And exercise 1 is crossed out
    And exercise 3 is crossed out
    When I open the settings
    Then the settings list 2 crossed-out exercises
    When I restore all crossed-out exercises
    Then the settings list no crossed-out exercises
