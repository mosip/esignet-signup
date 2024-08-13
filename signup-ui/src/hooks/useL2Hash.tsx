import { useLocation } from "react-router-dom";

export const useL2Hash = () => {
  const { hash } = useLocation();
  const hashString = hash.substring(1);

  const base64Regex =
    /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;

  const isValidHash = base64Regex.test(hashString);

  const nullParams = { state: null, code: null, ui_locales: null };

  if (isValidHash) {
    try {
      const decodedQueryString = atob(hashString);

      // Check if decoded string has the expected parameter format
      const paramsRegex = /^state=[^&]*&code=[^&]*&ui_locales=[^&]*$/;

      if (paramsRegex.test(decodedQueryString)) {
        const queryParams = new URLSearchParams(decodedQueryString);

        const state = queryParams.get("state");
        const code = queryParams.get("code");
        const ui_locales = queryParams.get("ui_locales");

        return { state, code, ui_locales };
      } else {
        return nullParams;
      }
    } catch (e) {
      return nullParams;
    }
  } else {
    return nullParams;
  }
};
