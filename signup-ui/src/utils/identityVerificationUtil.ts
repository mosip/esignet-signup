/**
 * Generates a unique state string for identity verification and stores related data in localStorage.
 * The state includes a timestamp, redirect URL, and expiry time.
 * @param {Object} stateObj - An object containing optional redirectUrl and expiryTime (in seconds).
 * @returns {string} - A base64 encoded string representing the unique state key.
 */
const randomKey = (prefix: string): string => {
  const timestamp = new Date().getTime();
  return `${prefix}-${timestamp}`;
};

/**
 * Clears all localStorage entries that start with the given key prefix.
 * @param keyPrefix - The prefix of the keys to be cleared from localStorage.
 */
const clearLocalStorageKey = (keyPrefix: string) => {
  for (let key in localStorage) {
    if (key.startsWith(keyPrefix)) {
      localStorage.removeItem(key);
    }
  }
};

/**
 * Generates a unique state string for identity verification and stores related data in localStorage.
 * The state includes a timestamp, redirect URL, and expiry time.
 * @param stateObj - An object containing optional redirectUrl and expiryTime (in seconds).
 * @returns {string} - A base64 encoded string representing the unique state key.
 */
export const generateState = (stateObj: any = {}): string => {
  const currentDate = new Date();
  const stateData = {
    timestamp: currentDate.getTime(),
    redirectUrl:
      stateObj.redirectUrl || window.location.origin + "/identity-verification",
    scope: stateObj.scope || "openid",
    claims: stateObj.claims || "",
    expiry: currentDate.getTime() + (stateObj.expiryTime || 600) * 1000, // default 10 minutes
    uiLocales: stateObj.uiLocales || (window as any)._env_.DEFAULT_LANG,
    acrValues:
      stateObj.acrValues ||
      "mosip:idp:acr:generated-code%20mosip:idp:acr:password%20mosip:idp:acr:linked-wallet%20mosip:idp:acr:knowledge",
  };

  const stateKey = randomKey("identity-verification");

  clearLocalStorageKey("identity-verification");
  localStorage.setItem(stateKey, btoa(JSON.stringify(stateData)));

  return btoa(stateKey);
};

/**
 * Retrieves and decodes the state data from localStorage using the provided encoded state key.
 * @param stateKeyEncoded - A base64 encoded string representing the unique state key.
 * @returns The decoded state data object or null if not found or expired.
 */
export const getStateData = (stateKeyEncoded: string): any | null => {
  try {
    const stateKey = atob(stateKeyEncoded);
    const stateDataEncoded = localStorage.getItem(stateKey);
    if (stateDataEncoded) {
      const stateData = JSON.parse(atob(stateDataEncoded));
      const currentTime = new Date().getTime();
      if (stateData.expiry && currentTime > stateData.expiry) {
        // State has expired
        localStorage.removeItem(stateKey);
        return null;
      }
      return stateData;
    }
    return null;
  } catch (error) {
    console.error("Error decoding state data:", error);
    return null;
  }
};
