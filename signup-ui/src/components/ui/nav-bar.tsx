import { forwardRef, useCallback, useEffect, useState } from "react";
import { Offline, PollingConfig } from "react-detect-offline";
import { useTranslation } from "react-i18next";

import { Language } from "~components/language";
import {
  EkycVerificationStore,
  errorBannerMessageSelector,
  useEkycVerificationStore,
} from "~pages/EkycVerificationPage/useEkycVerificationStore";
import { useSettings } from "~pages/shared/queries";


const NavBar = () => {
  const { errorBannerMessage } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        errorBannerMessage: errorBannerMessageSelector(state),
      }),
      []
    )
  );

  const [navbarStyle, setNavbarStyle] = useState(
    "sticky top-0 z-40 h-auto w-full border-gray-500 bg-white py-0 sm:px-0 shadow-md"
  );

  useEffect(() => {
    if (errorBannerMessage !== null && errorBannerMessage !== "") {
      setNavbarStyle(
        "sticky top-0 z-40 h-auto w-full border-gray-500 bg-white py-0 px-0 sm:px-0"
      );
    } else {
      setNavbarStyle(
        "sticky top-0 z-40 h-auto w-full border-gray-500 bg-white py-0 sm:px-0 shadow-md"
      );
    }
  }, [errorBannerMessage]);

  // Offline Polling: to check if device is offline or not
  const { t } = useTranslation();

  const [showBackOnline, setShowBackOnline] = useState<boolean>(false);

  const { data: settings, isLoading } = useSettings();

  const pollingConfig: PollingConfig = {
    timeout: settings?.response?.configs?.["offline.polling.timeout"] ?? 5000,
    interval: settings?.response?.configs?.["offline.polling.interval"] ?? 5000,
    enabled: settings?.response?.configs?.["offline.polling.enabled"] ?? true,
    url: settings?.response?.configs?.["offline.polling.url"] ?? "https://ipv4.icanhazip.com/"
  };

  let onlineTimeout: ReturnType<typeof setTimeout>;

  const handleOnchangeOffline = (showBackOnline: boolean) => {
    if (showBackOnline) {
      setShowBackOnline(true);
      onlineTimeout = setTimeout(() => {
        setShowBackOnline(false);
      }, settings?.response?.configs?.["online.polling.timeout"] ?? 5000);
      return;
    }

    clearTimeout(onlineTimeout);
  };

  return (
    <nav className={navbarStyle}>
      <div className="container flex h-full items-center justify-between px-[4rem] py-2 md:px-[0.5rem]">
        <div className="ltr:ml-1 ltr:sm:ml-8 rtl:mr-1 rtl:sm:mr-8">
          <img className="brand-logo" alt="brand_logo" />
        </div>
        <div className="flex ltr:mr-1 ltr:sm:mr-8 rtl:ml-1 rtl:sm:ml-8">
          <div className="mx-2 rtl:scale-x-[-1]">
            <Language />
          </div>
        </div>
      </div>
      <div className="row">
        <div>
          <Offline polling={pollingConfig} onChange={handleOnchangeOffline}>
            <div className="top-[70px] left-0 w-full h-auto py-3 opacity-100 flex justify-center items-center bg-[#FAEFEF] text-center">
              <p className="font-inter font-semibold text-[14px] leading-[17px] tracking-[0px] opacity-100 text-[#D52929]">
                {t("offline_polling_prompt")}
              </p>
            </div>
          </Offline>
          {showBackOnline &&
            <div className="top-[70px] left-0 w-full h-auto py-3 opacity-100 flex justify-center items-center bg-[#EAFAE4] text-center">
              <p className="font-inter font-semibold text-[14px] leading-[17px] tracking-[0px] opacity-100 text-[#419533]">
                {t("online_polling_prompt")}
              </p>
            </div>}
        </div>
      </div>
    </nav>
  );
};

export default forwardRef(NavBar);
