import { BROWSER_MIN_COMPATIBILITY } from "~constants/browsers";

/**
 * Compares two semantic versioning strings to determine if the current version is greater than or equal to the minimum version.
 *
 * @param {string} currentVersion  The current version string to compare, in the format 'major.minor.patch'.
 * @param {string} minVersion  The minimum version string to compare against, in the same format.
 * @returns {boolean}  Returns true if `currentVersion` is greater than or equal to `minVersion`, otherwise false.
 */
const compareVersions = (currentVersion: string, minVersion: string) => {
  const currentParts = currentVersion.split(".").map(Number);
  const minParts = minVersion.split(".").map(Number);

  for (let i = 0; i < minParts.length; i++) {
    if (currentParts[i] > minParts[i]) return true;
    if (currentParts[i] < minParts[i]) return false;
  }
  return true;
};

/**
 * Checks if the user's browser is compatible based on predefined minimum version requirements.
 * It tests the user agent string against a set of regular expressions for each browser to extract the version number.
 * Then, it compares the extracted version number with the minimum required version.
 *
 * @returns {boolean} Returns true if the current browser version is greater than or equal to the specified minimum version, otherwise false.
 */
export const isBrowserCompatible = () => {
  const userAgent = navigator.userAgent;
  const browserVersionRegex = {
    chrome: /Chrome\/(\d+\.\d+\.\d+\.\d+)/,
    firefox: /Firefox\/(\d+\.\d+)/,
    edge: /Edg\/(\d+\.\d+\.\d+\.\d+)/,
    safari: /Version\/(\d+\.\d+)/,
  };

  for (const [browser, regex] of Object.entries(browserVersionRegex)) {
    const match = userAgent.match(regex);
    if (match) {
      return compareVersions(match[1], BROWSER_MIN_COMPATIBILITY[browser]);
    }
  }
  return false;
};
