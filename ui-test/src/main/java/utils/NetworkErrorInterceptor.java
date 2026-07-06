package utils;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;

/**
 * Forces a given HTTP status code on backend calls whose URL contains a
 * configurable substring, using Selenium's CDP {@link NetworkInterceptor}.
 *
 * <p>This only works with a CDP-capable local driver (e.g. ChromeDriver). It is
 * NOT supported on BrowserStack RemoteWebDriver, so scenarios relying on it must
 * be tagged {@code @localOnly} and skipped when {@code runOnBrowserStack=true}.
 */
public class NetworkErrorInterceptor implements AutoCloseable {

	private static final Logger logger = Logger.getLogger(NetworkErrorInterceptor.class);

	private final NetworkInterceptor interceptor;

	/**
	 * @param driver       CDP-capable driver (ChromeDriver)
	 * @param uriSubstring only requests whose URI contains this substring are faulted
	 * @param statusCode   the HTTP status code to return for matching requests
	 */
	public NetworkErrorInterceptor(WebDriver driver, String uriSubstring, int statusCode) {
		this.interceptor = new NetworkInterceptor(driver,
				Route.matching(req -> req.getUri().contains(uriSubstring))
						.to(() -> req -> new HttpResponse()
								.setStatus(statusCode)
								.addHeader("Content-Type", "application/json")
								.setContent(Contents.utf8String("{\"error\":\"forced-" + statusCode + "\"}"))));
		logger.info("Network interception active: '" + uriSubstring + "' -> HTTP " + statusCode);
	}

	@Override
	public void close() {
		try {
			if (interceptor != null) {
				interceptor.close();
			}
		} catch (Exception e) {
			logger.warn("Failed to close network interceptor: " + e.getMessage());
		}
	}
}
