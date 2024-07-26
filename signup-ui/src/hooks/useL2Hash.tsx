import { useLocation } from "react-router-dom";

export const useL2Hash = () => {
  const { hash } = useLocation();

  const decodedQueryString = atob(hash.substring(1));

  const queryParams = new URLSearchParams(decodedQueryString);

  const state = queryParams.get("state");
  const code = queryParams.get("code");
  const ui_locales = queryParams.get("ui_locales");

  return { state, code, ui_locales };
};
