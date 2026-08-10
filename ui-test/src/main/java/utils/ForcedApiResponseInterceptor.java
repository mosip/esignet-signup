package utils;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;

// Forces a fixed status code and JSON body on backend calls matching a URL substring.
// CDP-only, so scenarios using it must be tagged @localOnly (no BrowserStack RemoteWebDriver).
public class ForcedApiResponseInterceptor implements AutoCloseable {

	private static final Logger logger = Logger.getLogger(ForcedApiResponseInterceptor.class);

	private final NetworkInterceptor interceptor;

	public ForcedApiResponseInterceptor(WebDriver driver, String uriSubstring, int statusCode, String jsonBody) {
		this(driver, statusCode, Map.of(uriSubstring, jsonBody));
	}

	// All rules share one route: two interceptors on the same driver clash, as the last Fetch.enable wins
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
