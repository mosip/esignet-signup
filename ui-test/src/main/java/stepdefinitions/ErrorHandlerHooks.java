package stepdefinitions;

import org.testng.SkipException;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.EsignetConfigManager;
import utils.ExtentReportManager;

/**
 * Hooks that guard {@code @localOnly} scenarios (e.g. the CDP-based forced 5XX
 * tests) so they are skipped instead of failing when the suite runs against
 * BrowserStack, where Selenium CDP network interception is not available.
 *
 * <p>Kept in its own glue class (holding no page objects / driver) so it can run
 * early without forcing premature instantiation of driver-backed step defs.
 */
public class ErrorHandlerHooks {

	// Low order so this runs before the driver-setup @Before (default 10000):
	// when skipped on BrowserStack, no remote session is created.
	@Before(value = "@localOnly", order = 100)
	public void skipLocalOnlyScenariosOnBrowserStack(Scenario scenario) {
		boolean runOnBrowserStack = Boolean
				.parseBoolean(EsignetConfigManager.getproperty("runOnBrowserStack"));
		if (runOnBrowserStack) {
			// Create the report entry up-front so the shared @After reporting has a
			// valid test object for this skipped scenario.
			ExtentReportManager.createTest(scenario.getName());
			ExtentReportManager.getTest()
					.skip("⚠️ Requires local Chrome with CDP network interception. Skipped on BrowserStack. "
							+ "Run with runOnBrowserStack=false to execute.");
			throw new SkipException(
					"Requires local Chrome (CDP). Skipped on BrowserStack: " + scenario.getName());
		}
	}
}
