import { PageLayout } from "~layouts/PageLayout";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { ReactComponent as SomethingWentWrongSvg } from "~assets/svg/something-went-wrong.svg";
import {
  EKYC_VERIFICATION,
  RESET_PASSWORD,
  SIGNUP_ROUTE,
} from "~constants/routes";
import { Button } from "~components/ui/button";
import { generateState } from "~utils/identityVerificationUtil";
import { useEkycVerificationStore } from "~pages/EkycVerificationPage/useEkycVerificationStore";
import { useResetPasswordStore } from "~pages/ResetPasswordPage/useResetPasswordStore";
import { useSettings } from "~pages/shared/queries";
import { useSignUpStore } from "~pages/SignUpPage/useSignUpStore";

export const LandingPage = () => {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();

  const { data: settings } = useSettings();

  const { hash: fromSignInHash } = useLocation();
  const resetSignupStore = useSignUpStore.getState().reset;
  const resetForgotPasswordStore = useResetPasswordStore.getState().reset;
  const resetEkycVerificationStore = useEkycVerificationStore.getState().reset;

  const handleResetPassword = (e: any) => {
    e.preventDefault();
    resetForgotPasswordStore();
    navigate(`${RESET_PASSWORD}${fromSignInHash}`);
  };

  const handleRegister = (e: any) => {
    e.preventDefault();
    resetSignupStore();
    navigate(`${SIGNUP_ROUTE}${fromSignInHash}`);
  };

  const handleVerifyIdentity = (e: any) => {
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
    <PageLayout
      className="h-[calc(100vh-13vh)] w-full items-center justify-center p-16 px-32 sm:px-[30px]"
      childClassName="h-full"
    >
      <div className="flex h-full w-full flex-col items-center justify-center gap-y-8 rounded-xl bg-white shadow-lg md:shadow-none">
        <SomethingWentWrongSvg />
        <div className="flex flex-col items-center gap-y-2">
          <h1 className="text-center text-2xl">{t("landing_page_title")}</h1>
          <p className="text-center text-gray-500">
            {t("landing_page_description")}
          </p>
        </div>
        <div className="flex w-full flex-row items-center justify-center gap-x-2 md:flex-col">
          <Button
            className="h-[52px] w-[250px] border-[2px] border-primary bg-white text-primary hover:text-primary/80 md:mb-3 md:w-full"
            id="reset-password-button"
            name="reset-password-button"
            variant="outline"
            onClick={handleResetPassword}
          >
            {t("reset_password")}
          </Button>
          <Button
            className="h-[52px] w-[250px] md:w-full"
            id="register-button"
            name="register-button"
            onClick={handleRegister}
          >
            {t("register")}
          </Button>
          <Button
            className="h-[52px] w-[250px] border-[2px] border-primary bg-white text-primary hover:text-primary/80 md:mt-3 md:w-full"
            id="verify-identity-button"
            name="verify-identity-button"
            variant="outline"
            onClick={handleVerifyIdentity}
          >
            {t("verify_identity")}
          </Button>
        </div>
      </div>
    </PageLayout>
  );
};
