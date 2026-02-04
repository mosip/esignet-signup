/**
 * Generates a unique state string for identity verification and stores related data in localStorage.
 * The state includes a timestamp, redirect URL, and expiry time.
 * @param {Object} stateObj - An object containing optional redirectUrl and expiryTime (in seconds).
 * @returns {string} - A base64 encoded string representing the unique state key.
 */
const randomKey = (prefix: string): string => {
  const randomUid = crypto.randomUUID().replace(/-/g, "");
  return `${prefix}-${randomUid}`;
};

/**
 * Encodes or decodes a string using Base64.
 * @param str String to be encoded or decoded
 * @param encode Whether to encode (true) or decode (false)
 * @returns Encoded or decoded string
 */
const encoderDecoder = (str: string, encode: boolean = false): string => {
  if (encode) {
    // Encode: string -> base64
    return btoa(unescape(encodeURIComponent(str)));
  }
  // Decode: base64 -> string
  return decodeURIComponent(escape(atob(str)));
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
 * Removes a specific token from a %20-separated (URL-encoded) list.
 * Preserves colons (:) and other safe characters when re-encoding.
 *
 * @param {string} acr - The input string (URL-encoded, tokens separated by %20).
 * @param {string} tokenToRemove - The exact token to remove (plain text, not encoded).
 * @returns {string} - The URL-encoded string with the token removed.
 */
const removeEncodedToken = (acr: string, tokenToRemove: string): string => {
  if (acr === "") {
    return "mosip:idp:acr:generated-code%20mosip:idp:acr:password%20mosip:idp:acr:linked-wallet%20mosip:idp:acr:knowledge";
  }
  // 1) Decode to operate on plain text
  const decoded = decodeURIComponent(acr); // %20 -> ' '

  // 2) Split on whitespace (handles multiple spaces just in case)
  const tokens = decoded.trim().split(/\s+/);

  // 3) Filter out the target token (exact match)
  const filtered = tokens.filter((t) => t !== tokenToRemove);

  // 4) Re-encode; keep ':' readable, optionally also keep '-' safe
  // encodeURIComponent encodes ':', so we bring it back for readability.
  return encodeURIComponent(filtered.join(" "))
    .replace(/%3A/g, ":")
    .replace(/%2D/g, "-"); // optional: keep dashes readable
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
    acrValues: removeEncodedToken(
      stateObj.acrValues || "",
      "mosip:idp:acr:id-token"
    ),
  };

  const stateKey = randomKey("identity-verification");

  clearLocalStorageKey("identity-verification");
  localStorage.setItem(stateKey, encoderDecoder(JSON.stringify(stateData), true));

  return encoderDecoder(stateKey, true);
};

/**
 * Retrieves and decodes the state data from localStorage using the provided encoded state key.
 * @param stateKeyEncoded - A base64 encoded string representing the unique state key.
 * @returns The decoded state data object or null if not found or expired.
 */
export const getStateData = (stateKeyEncoded: string): any | null => {
  try {
    const stateKey = encoderDecoder(stateKeyEncoded);
    const stateDataEncoded = localStorage.getItem(stateKey);
    if (stateDataEncoded) {
      const stateData = JSON.parse(encoderDecoder(stateDataEncoded));
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
