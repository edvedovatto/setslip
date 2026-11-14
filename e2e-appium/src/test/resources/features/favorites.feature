@regression
Feature: Favorite slips

  Scenario: There are no favorites at first
    Then there is no favorites badge
    When I open the favorites
    Then no favorite slips are listed

  @smoke
  Scenario: Favorite the slip
    Given a printed slip for chest
    When I favorite the slip
    Then the slip is marked as favorite
    And the favorites badge shows 1

  Scenario: Remove a favorite
    Given a favorite slip for chest
    When I unfavorite the slip
    Then the slip is not marked as favorite
    And there is no favorites badge

  Scenario: Print a favorite again
    Given a favorite slip for chest
    And a printed slip for back
    When I open the favorites
    And I use the favorite slip for chest
    Then the slip has the same exercises as the favorite
    And the slip is variation A
    And the slip is marked as favorite

  Scenario: Filter the favorites by group
    Given a favorite slip for chest
    And a favorite slip for back
    When I open the favorites
    Then the favorites list 2 slips
    When I filter the favorites by back
    Then the favorites list only the slip for back

  Scenario: A filter that matches no favorite
    Given a favorite slip for chest
    When I open the favorites
    And I filter the favorites by glutes
    Then no favorite slips are listed
