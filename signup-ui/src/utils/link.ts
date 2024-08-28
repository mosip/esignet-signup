export const getSignInRedirectURL = (
  redirectUrl: string | undefined,
  hash: string,
  defaultPath: string
): string => {
  const locale = localStorage.getItem("esignet-signup-language");
  if (!!hash) {
    const signInQueryParams = replaceUILocales(hash, locale);
    return redirectUrl + "?" + signInQueryParams?.toString();
  }
  return defaultPath + "?ui_locales=" + locale;
};

export const replaceUILocales = (
  hash: string,
  locale: string | null
): URLSearchParams | undefined => {

  const base64Regex = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;
  if (base64Regex.test(hash.substring(1))) {
    // Convert the decoded string to JSON
    const decodedBase64 = atob(hash.substring(1));
    const urlSearchParams = new URLSearchParams(decodedBase64);

    if (urlSearchParams) {
      // Convert the URLSearchParams to a JSON object
      const jsonObject: Record<string, string> = {};
      urlSearchParams.forEach((value, key) => {
        jsonObject[key] = value;
        // Assign the current i18n language to the ui_locales
        if (key === "ui_locales") {
          jsonObject[key] = locale || value;
        }
      });

      Object.entries(jsonObject).forEach(([key, value]) => {
        urlSearchParams.set(key, value);
      });

      return urlSearchParams;
    }
  }
  else {
    return undefined;
  }
};
