import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import {
  EKYC_VERIFICATION,
  SIGNUP_ROUTE,
} from "~constants/routes";
import { Card, CardContent } from "~components/ui/card";
import NavBar from "~components/ui/nav-bar";
import LandingFooter from "~components/ui/landing-footer";
import { PageLayout } from "~layouts/PageLayout";
import { generateState } from "~utils/identityVerificationUtil";
import { useEkycVerificationStore } from "~pages/EkycVerificationPage/useEkycVerificationStore";
import { useSettings } from "~pages/shared/queries";
import { useSignUpStore } from "~pages/SignUpPage/useSignUpStore";

export const LandingPage = () => {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();

  const { data: settings } = useSettings();

  const { hash: fromSignInHash } = useLocation();
  const resetSignupStore = useSignUpStore.getState().reset;
  const resetEkycVerificationStore = useEkycVerificationStore.getState().reset;

  const handleSetupAccount = (e: any) => {
    e.preventDefault();
    resetSignupStore();
    navigate(`${SIGNUP_ROUTE}${fromSignInHash}`);
  };

  const handleEkycVerification = (e: any) => {
    e.preventDefault();
    resetEkycVerificationStore();
    const rpConfig = settings?.response?.configs["rp.config"];
    const state = generateState({
      redirectUrl: rpConfig?.redirect_uri_verification,
      expiryTime: rpConfig?.expiry_time,
      scope: rpConfig?.scope,
      acrValues: rpConfig?.acr_values,
      uiLocales: i18n.language,
    });
    navigate(`${EKYC_VERIFICATION}${fromSignInHash}?state=${state}`);
  };

  return (
    <div className="flex min-h-screen flex-col">
      {/* NavBar */}
      <NavBar />
      
      <PageLayout childClassName="my-5 flex justify-center">
        <div className="w-full max-w-4xl">
          <div className="mb-8 flex justify-center">
            <div className="rounded-md bg-primary/10 px-4 py-2 text-sm font-medium text-primary">
              {t("secure_login")}
            </div>
          </div>

          <h1 className="mb-4 text-center text-4xl font-bold text-primary md:text-3xl sm:text-2xl">
            {t("landing_page_title")}
          </h1>

          <p className="mb-12 text-center text-lg text-primary-light md:text-base">
            {t("landing_page_subtitle")}
          </p>

          <div className="mb-12 grid grid-cols-2 gap-6 sm:grid-cols-1">
            <Card
              className="cursor-pointer border-2 border-gray-200 transition-all hover:border-primary hover:shadow-lg"
              onClick={handleSetupAccount}
            >
              <CardContent className="flex flex-col items-center p-8 text-center">
                <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-xl">
                  <img 
                    src="/images/user_register.png" 
                    alt="Setup Account"
                    className="h-12 w-12"
                  />
                </div>
                
                <h2 className="mb-3 text-xl font-semibold text-primary">
                  {t("setup_new_account")}
                </h2>
                
                <p className="text-sm text-primary-light">
                  {t("setup_account_description")}
                </p>
              </CardContent>
            </Card>

            <Card
              className="cursor-pointer border-2 border-gray-200 transition-all hover:border-primary hover:shadow-lg"
              onClick={handleEkycVerification}
            >
              <CardContent className="flex flex-col items-center p-8 text-center">
                <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-xl">
                  <img 
                    src="/images/kycVerification.png" 
                    alt="eKYC Verification"
                    className="h-12 w-12"
                  />
                </div>
                
                <h2 className="mb-3 text-xl font-semibold text-primary">
                  {t("proceed_with_ekyc")}
                </h2>
                
                <p className="text-sm text-primary-light">
                  {t("ekyc_description")}
                </p>
              </CardContent>
            </Card>
          </div>

          <div className="flex flex-col items-center">
            <p className="mb-4 text-center text-sm text-primary-hover">
              {t("trusted_by")}
            </p>
            <div className="flex flex-wrap items-center justify-center gap-6">
              <div className="flex items-center gap-2">
                <img 
                  src="/images/ssl_icon.svg" 
                  alt="SSL Secured"
                  className="h-4 w-4"
                />
                <span className="text-xs text-primary-light">{t("ssl_secured")}</span>
              </div>
              <div className="flex items-center gap-2">
                <img 
                  src="/images/encrypt_icon.svg" 
                  alt="Encrypted"
                  className="h-4 w-4"
                />
                <span className="text-xs text-primary-light">{t("encrypted")}</span>
              </div>
              <div className="flex items-center gap-2">
                <img 
                  src="/images/verified_icon.svg" 
                  alt="Government Verified"
                  className="h-4 w-4"
                />
                <span className="text-xs text-primary-light">{t("government_verified")}</span>
              </div>
            </div>
          </div>
        </div>
      </PageLayout>

      <LandingFooter />
    </div>
  );
};
