import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import { ReactComponent as WarningIconSvg } from "~assets/svg/warning-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent } from "~components/ui/step";
import { EKYC_VERIFICATION } from "~constants/routes";
import { getSignInRedirectURLV2 } from "~utils/link";
import { useSettings } from "~pages/shared/queries";
import { useEkycVerificationStore } from "~pages/EkycVerificationPage/useEkycVerificationStore";
import { generateState } from "~utils/identityVerificationUtil";

interface AccountRegistrationStatusLayoutProps {
  status: "success" | "warning" | "failed";
  message: string;
}

export const AccountRegistrationStatusLayout = ({
  status,
  message,
}: AccountRegistrationStatusLayoutProps) => {
  const { t, i18n } = useTranslation();
  const { data: settings } = useSettings();
  const { hash: fromSignInHash, search } = useLocation();
  const navigate = useNavigate();
  const resetEkycVerificationStore = useEkycVerificationStore.getState().reset;
  const rpConfig = settings?.response?.configs["rp.config"];

  const handleAction = (e: any) => {
    e.preventDefault();
    window.location.href = getSignInRedirectURLV2(
      rpConfig?.redirect_uri_signin,
      fromSignInHash,
      search,
      "/"
    );
  };

  const handleVerifyIdentity = (e: any) => {
    e.preventDefault();
    resetEkycVerificationStore();
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
    <Step className={status === "success" ? "overflow-hidden" : ""}>
      <StepContent className={status === "success" ? "p-0" : ""}>
        {status === "success" && (
          <div className="verification-success-header w-full py-6 px-6 text-center">
            <div className="mb-4 flex justify-center">
              <img
                className="verification-status-icon h-20 w-20"
                alt="Success"
              />
            </div>
            <div className="text-center">
              <h1 className="text-3xl font-bold text-white md:text-2xl sm:text-xl">
                {t("congratulations")}
              </h1>
              <h2 className="mt-2 text-xl font-semibold text-white md:text-lg sm:text-base">
                {t("account_created_successfully")}
              </h2>
            </div>
          </div>
        )}

        {/* Warning/Failed Header */}
        {status !== "success" && (
          <div className="flex flex-col items-center gap-4 py-4">
            {status === "warning" ? (
              <WarningIconSvg />
            ) : (
              <FailedIconSvg />
            )}
            <div className="text-center text-lg font-semibold">
              <h1>
                {status === "warning"
                  ? t("signup_pending")
                  : t("signup_failed")}
              </h1>
            </div>
          </div>
        )}

        <div className={status === "success" ? "verification-success-content flex flex-col items-center px-6 py-6 text-center sm:px-8" : "px-6"}>
          {status === "success" && (
            <>
              <p className="mb-3 text-center text-base text-primary-light md:text-sm">
                {t("account_created_success_description")}
              </p>
              <p className="mb-6 text-center text-base font-medium text-primary md:text-sm">
                {t("account_created_visa_id")}
              </p>
            </>
          )}

          {/* Action Buttons */}
          <div className="flex w-full max-w-md flex-col gap-2">
            <Button
              id="success-continue-button"
              className="h-16 w-full"
              onClick={handleAction}
            >
              {fromSignInHash ? t("login") : t("okay")}
            </Button>
            {status === "success" && (
              <Button
                id="verify-identity-button"
                className="h-16 w-full border-primary bg-white text-primary hover:bg-primary/5 hover:text-primary"
                variant="outline"
                onClick={handleVerifyIdentity}
              >
                {t("proceed_to_verification")}
              </Button>
            )}
          </div>
        </div>
      </StepContent>
    </Step>
  );
};
