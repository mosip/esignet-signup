export const getSignInRedirectURL = (
  redirectUrl: string | undefined,
  hash: string,
  defaultPath: string
): string => {
  const locale = localStorage.getItem("esignet-signup-language");
  if (!!hash) {
    const signInQueryParams = replaceUILocales(hash, locale);
    return redirectUrl + "?" + signInQueryParams.toString();
  }
  return defaultPath + "?ui_locales=" + locale;
};

export const replaceUILocales = (
  hash: string,
  locale: string | null
): URLSearchParams => {
  // Convert the decoded string to JSON
  const decodedBase64 = atob(hash.substring(1));

  var urlSearchParams = new URLSearchParams(decodedBase64);

  // Convert the decoded string to JSON
  var jsonObject: Record<string, string> = {};
  urlSearchParams.forEach(function (value, key) {
    jsonObject[key] = value;
    // Assign the current i18n language to the ui_locales
    if (key === "ui_locales") {
      jsonObject[key] = locale || value;
    }
  });

  // Convert the JSON back to decoded string
  Object.entries(jsonObject).forEach(([key, value]) => {
    urlSearchParams.set(key, value);
  });

  return urlSearchParams;
};
