import Bowser, { BROWSER_MAP } from "bowser";
import { findKey } from "lodash";

import { BROWSER_MIN_COMPATIBILITY } from "~constants/browsers";
import { compareVersions } from "~utils/browser";

export const checkBrowserCompatible = (
  minBrowserCompatibility: { [key: string]: string } = BROWSER_MIN_COMPATIBILITY
) => {
  const browserInfo = Bowser.parse(window.navigator.userAgent);

  const browserName = findKey(
    BROWSER_MAP,
    (b) => b === browserInfo.browser.name
  );

  if (!browserName) return false;

  const minBrowserVersion = minBrowserCompatibility[browserName];
  const currentBrowserVersion = browserInfo.browser.version;

  if (minBrowserVersion === "" || !currentBrowserVersion) return false;

  const isEligible = compareVersions(currentBrowserVersion, minBrowserVersion);

  if (isEligible) {
    return true;
  }

  return false;
};
