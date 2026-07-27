package utils;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;

/**
 * Forces a fixed HTTP status code and JSON body on backend calls whose URL
 * contains a configurable substring, using Selenium's CDP
 * {@link NetworkInterceptor}.
 *
 * <p>Unlike {@link NetworkErrorInterceptor} (which fakes a raw HTTP <em>error</em>
 * status with a generic body), this returns a caller-supplied JSON payload,
 * defaulting to HTTP 200, so application-level error envelopes can be simulated
 * &mdash; e.g. a signup {@code verify-challenge} response carrying
 * {@code {"errors":[{"errorCode":"invalid_transaction"}]}} to drive the
 * transaction-timeout popup without waiting for the real (~5 minute) expiry.
 *
 * <p>CDP-only: works with a local ChromeDriver, NOT BrowserStack
 * RemoteWebDriver, so scenarios relying on it must be tagged {@code @localOnly}.
 */
public class ForcedApiResponseInterceptor implements AutoCloseable {

	private static final Logger logger = Logger.getLogger(ForcedApiResponseInterceptor.class);

	private final NetworkInterceptor interceptor;

	/**
	 * @param driver       CDP-capable driver (ChromeDriver)
	 * @param uriSubstring only requests whose URI contains this substring are faulted
	 * @param statusCode   the HTTP status code to return for matching requests
	 * @param jsonBody     the JSON response body to return for matching requests
	 */
	public ForcedApiResponseInterceptor(WebDriver driver, String uriSubstring, int statusCode, String jsonBody) {
		this(driver, statusCode, Map.of(uriSubstring, jsonBody));
	}

	/**
	 * Stubs several endpoints in a single CDP session. Two separate
	 * {@link NetworkInterceptor} instances on the same driver would clash (the
	 * last {@code Fetch.enable} wins), so all rules are combined into one route.
	 *
	 * @param driver                    CDP-capable driver (ChromeDriver)
	 * @param statusCode                the HTTP status to return for every matched request
	 * @param uriSubstringToJsonBody    map of URI substring -&gt; JSON response body
	 */
	public ForcedApiResponseInterceptor(WebDriver driver, int statusCode, Map<String, String> uriSubstringToJsonBody) {
		Route route = null;
		for (Map.Entry<String, String> rule : uriSubstringToJsonBody.entrySet()) {
			final String uriSubstring = rule.getKey();
			final String jsonBody = rule.getValue();
			Route matched = Route.matching(req -> req.getUri().contains(uriSubstring))
					.to(() -> req -> new HttpResponse()
							.setStatus(statusCode)
							.addHeader("Content-Type", "application/json")
							.setContent(Contents.utf8String(jsonBody)));
			route = (route == null) ? matched : Route.combine(route, matched);
		}
		this.interceptor = new NetworkInterceptor(driver, route);
		logger.info("Forced API responses active: HTTP " + statusCode + " for " + uriSubstringToJsonBody.keySet());
	}

	@Override
	public void close() {
		try {
			if (interceptor != null) {
				interceptor.close();
			}
		} catch (Exception e) {
			logger.warn("Failed to close forced API response interceptor: " + e.getMessage());
		}
	}
}
