@accessibility @regression
Feature: Accessibility basics

  Background:
    Given the app is in English
    And a printed slip for chest

  Scenario: Icon-only controls are described to screen readers
    Then these controls are described to screen readers:
      | settings      | SETTINGS      |
      | favorites     | FAVORITES     |
      | another slip  | ANOTHER       |
      | sound light   | SOUND         |
      | how to        | HOW TO        |
      | favorite slip | favorite slip |
      | share slip    | share slip    |

  Scenario: Panel controls follow the Android touch target guideline
    Then every control on the panel has a touch target of at least 48dp

  Scenario: Every control meets the WCAG minimum target size
    Then every control on the screen has a touch target of at least 24dp

  @known-issue
  Scenario: Slip and header controls follow the Android touch target guideline
    Then these controls have a touch target of at least 48dp:
      | sound light   |
      | how to        |
      | favorite slip |
      | share slip    |

  Scenario: Exercises are exposed as checkboxes
    Then exercise 1 is exposed as an unchecked checkbox
    When I check exercise 1
    Then exercise 1 is exposed as a checked checkbox
    When I cross out exercise 2
    Then exercise 2 is exposed as disabled

  @animations
  Scenario: The printer announces what it is doing
    Then the printer announces "slip ready"
    When I press another
    Then the printer announces "replacing slip"
    When the slip is ready
    Then the printer announces "slip ready"
