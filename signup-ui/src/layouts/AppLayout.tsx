import { useCallback } from "react";
import { Outlet } from "react-router-dom";

import Footer from "~components/ui/footer";
import NavBar from "~components/ui/nav-bar";
import {
  EkycVerificationStore,
  errorBannerMessageSelector,
  useEkycVerificationStore,
} from "~pages/EkycVerificationPage/useEkycVerificationStore";

export const AppLayout = () => {
  const { errorBannerMessage } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        errorBannerMessage: errorBannerMessageSelector(state),
      }),
      []
    )
  );

  return (
    <div className="flex min-h-screen flex-col">
      <NavBar />
      <div className="relative flex flex-grow flex-col sm:bg-white">
        {errorBannerMessage !== null && errorBannerMessage !== "" && (
          <div className="error-banner">{errorBannerMessage}</div>
        )}
        <Outlet />
        <Footer />
      </div>
    </div>
  );
};
