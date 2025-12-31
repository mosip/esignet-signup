import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { ReactComponent as WarningIconSvg } from "~assets/svg/warning-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent } from "~components/ui/step";
import { EKYC_VERIFICATION } from "~constants/routes";
import { getSignInRedirectURLV2, generateState } from "~utils/link";
import { useSettings } from "~pages/shared/queries";
import { useEkycVerificationStore } from "~pages/EkycVerificationPage/useEkycVerificationStore";

interface AccountRegistrationStatusLayoutProps {
  status: "success" | "warning" | "failed";
  message: string;
}

export const AccountRegistrationStatusLayout = ({
  status,
  message,
}: AccountRegistrationStatusLayoutProps) => {
  const { t } = useTranslation();
  const { data: settings } = useSettings();
  const { hash: fromSignInHash, search } = useLocation();
  const navigate = useNavigate();
  const resetEkycVerificationStore = useEkycVerificationStore.getState().reset;

  const handleAction = (e: any) => {
    e.preventDefault();
    window.location.href = getSignInRedirectURLV2(
      settings?.response.configs["signin.redirect-url"],
      fromSignInHash,
      search,
      "/signup"
    );
  };

  const handleVerifyIdentity = (e: any) => {
    e.preventDefault();
    resetEkycVerificationStore();
    const state = generateState();
    navigate(`${EKYC_VERIFICATION}${fromSignInHash}?state=${state}`);
  };

  return (
    <Step>
      <StepContent>
        <div className="flex flex-col items-center gap-4 py-4">
          {status === "success" ? (
            <SuccessIconSvg />
          ) : status === "warning" ? (
            <WarningIconSvg />
          ) : (
            <FailedIconSvg />
          )}
          <div className="text-center text-lg font-semibold">
            {status === "success" ? (
              <>
                <h1>{t("congratulations")}</h1>
                <h2>{t("account_created_successfully")}</h2>
              </>
            ) : (
              <h1>
                {status === "warning"
                  ? t("signup_pending")
                  : t("signup_failed")}
              </h1>
            )}
          </div>
          <p className="text-center text-gray-500">{message}</p>
        </div>
        <div className="flex w-full flex-row items-center justify-center gap-x-2 md:flex-col">
          <Button
          id="success-continue-button"
          className="my-4 h-16 md:mb-3 w-full"
          onClick={handleAction}
        >
          {fromSignInHash ? t("login") : t("okay")}
        </Button>
        <Button
          id="verify-identity-button"
          className="my-4 h-16 border-primary bg-white text-primary hover:text-primary/80 md:mt-3 w-full"
          variant="outline"
          onClick={handleVerifyIdentity}
        >
          {t("proceed_to_verification")}
        </Button>
        </div>
      </StepContent>
    </Step>
  );
};
