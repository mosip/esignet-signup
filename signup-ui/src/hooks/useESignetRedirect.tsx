import { useLocation } from "react-router-dom";

import { SIGNUP_ROUTE } from "~constants/routes";
import { getSignInRedirectURLV2 } from "~utils/link";
import { useSettings } from "~pages/shared/queries";

export const useESignetRedirect = () => {
  const { data: settings } = useSettings();
  const { hash: fromSignInHash, search } = useLocation();

  const handleRedirectToSignIn = () => {
    window.onbeforeunload = null;
    window.location.href = getSignInRedirectURLV2(
      settings?.response?.configs["signin.redirect-url"],
      fromSignInHash,
      search,
      SIGNUP_ROUTE
    );
  };

  return { handleRedirectToSignIn };
};
