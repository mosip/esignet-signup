import { BROWSER_MIN_COMPATIBILITY } from "~constants/browsers";
import { compareVersions } from "~utils/browser";

/**
 * Checks if the browser is compatible based on its version.
 *
 * This function gets the user agent string and defines a regular expression for each browser to extract its version.
 * It then loops over the regular expressions, and if it finds a match in the user agent string, it compares the extracted version with the minimum compatible version for that browser.
 * If the browser version is greater than or equal to the minimum compatible version, it returns true.
 * If no match is found for any browser, it returns false.
 *
 * @returns {boolean} - True if the browser is compatible, false otherwise.
 */
export const checkBrowserCompatible = () => {
  const userAgent = navigator.userAgent;
  const browserVersionRegex = {
    chrome: /Chrome\/(\d+\.\d+\.\d+\.\d+)/,
    firefox: /Firefox\/(\d+\.\d+)/,
    edge: /Edg\/(\d+\.\d+\.\d+\.\d+)/,
    safari: /Version\/(\d+\.\d+)/,
  };

  for (const [browser, regex] of Object.entries(browserVersionRegex)) {
    const browserVersionMatch = userAgent.match(regex);
    if (browserVersionMatch) {
      return compareVersions(
        browserVersionMatch[1],
        BROWSER_MIN_COMPATIBILITY[browser]
      );
    }
  }
  return false;
};
