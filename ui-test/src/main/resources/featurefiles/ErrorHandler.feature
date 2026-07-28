Feature: Esignet Error Handler Page
  Verifying the "Something went wrong" error handler page shown for HTTP 5XX
  backend responses, and that its content is localized on language switch.

  # Loads the error route directly, so title and description both come from the localized keys
  @smoke @errorHandlerPage
  Scenario: Verify the error handler page content and language switch
    Given user navigates directly to the error handler page
    When user switches the error handler page language to "en"
    Then verify the error handler page displays title in "en" language
    And verify the error handler page displays description in "en" language
    When user switches the error handler page language to "km"
    Then verify the error handler page displays title in "km" language
    And verify the error handler page displays description in "km" language

  # Status code reaches the error page, so the title is the HTTP reason phrase and stays unlocalized
  @errorHandlerHttp @localOnly
  Scenario Outline: Verify the error handler page for HTTP <code> response and language switch
    Given the signup settings API is forced to return HTTP <code>
    When user navigates to the signup portal to trigger the error
    Then verify the error handler page title shows the reason phrase "<reasonPhrase>"
    When user switches the error handler page language to "en"
    Then verify the error handler page displays description in "en" language
    And verify the error handler page title shows the reason phrase "<reasonPhrase>"
    When user switches the error handler page language to "km"
    Then verify the error handler page displays description in "km" language
    And verify the error handler page title shows the reason phrase "<reasonPhrase>"

    Examples:
      | code | reasonPhrase           |
      | 400  | Bad Request            |
      | 403  | Forbidden              |
      | 404  | Not Found              |
      | 405  | Method Not Allowed     |
      | 415  | Unsupported Media Type |
      | 500  | Internal Server Error  |
      | 502  | Bad Gateway            |
      | 503  | Service Unavailable    |
      | 504  | Gateway Timeout        |
