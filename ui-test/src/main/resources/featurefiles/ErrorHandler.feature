Feature: Esignet Error Handler Page
  Verifying the "Something went wrong" error handler page shown for HTTP 5XX
  backend responses, and that its content is localized on language switch.

  # Runs everywhere (incl. BrowserStack). Loads the error route directly, so no
  # HTTP status code is present in router state and both the title and the
  # description come from the localized keys (something_went_wrong /
  # something_went_wrong_detail). Language switch must update both lines.
  @smoke @errorHandlerPage
  Scenario: Verify the error handler page content and language switch
    Given user navigates directly to the error handler page
    When user switches the error handler page language to "en"
    Then verify the error handler page displays title in "en" language
    And verify the error handler page displays description in "en" language
    When user switches the error handler page language to "km"
    Then verify the error handler page displays title in "km" language
    And verify the error handler page displays description in "km" language

  # High-fidelity, local Chrome only (@localOnly is skipped on BrowserStack).
  # A real 4XX/5XX is forced on the /settings API via CDP so the app's axios
  # interceptor redirects to the error page WITH the status code; the title then
  # renders the HTTP reason phrase (e.g. Bad Request / Not Found / Internal Server
  # Error) which is NOT localized, while only the description changes on language
  # switch. The single outline reuses the same steps for every handled status
  # code (see api.service.ts: [400, 403, 404, 405, 415, 500, 502, 503, 504]).

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
      | 504  | Gateway Timeout        |
