import { useLocation } from "react-router-dom";

import { SIGNUP_ROUTE } from "~constants/routes";
import { getSignInRedirectURL } from "~utils/link";
import { useSettings } from "~pages/shared/queries";

export const useESignetRedirect = () => {
  const { data: settings } = useSettings();
  const { hash: fromSignInHash } = useLocation();

  const handleRedirectToSignIn = () => {
    window.onbeforeunload = null;
    window.location.href = getSignInRedirectURL(
      settings?.response?.configs["signin.redirect-url"],
      fromSignInHash,
      SIGNUP_ROUTE
    );
  };

  return { handleRedirectToSignIn };
};
