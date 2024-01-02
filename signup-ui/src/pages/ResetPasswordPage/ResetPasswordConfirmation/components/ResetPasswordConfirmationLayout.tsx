import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent } from "~components/ui/step";
import { getSignInRedirectURL } from "~utils/link";
import { useSettings } from "~pages/shared/queries";
import { RESET_PASSWORD } from "~constants/routes";

interface ResetPasswordConfirmationLayoutProps {
  status: "success" | "failed";
  message: string;
}

export const ResetPasswordConfirmationLayout = ({
  status,
  message,
}: ResetPasswordConfirmationLayoutProps) => {
  const { t } = useTranslation();
  const { data: settings } = useSettings();
  const { hash: fromSignInHash } = useLocation();

  const handleAction = (e: any) => {
    e.preventDefault();
    window.location.href = getSignInRedirectURL(
      settings?.response.configs["signin.redirect-url"],
      fromSignInHash,
      RESET_PASSWORD
    );
  };

  return (
    <Step>
      <StepContent>
        <div className="flex flex-col items-center gap-4 py-4">
          {status === "success" ? <SuccessIconSvg /> : <FailedIconSvg />}
          <div className="text-center text-lg font-semibold">
            {status === "success" ? (
              <h1>{t("password_reset_confirmation")}</h1>
            ) : (
              <h1>{t("password_reset_failed")}</h1>
            )}
          </div>
          <p className="break-all text-center text-muted-neutral-gray">
            {message}
          </p>
        </div>
        <Button className="my-4 h-16 w-full" onClick={handleAction}>
          {status === "success"
            ? fromSignInHash
              ? t("login")
              : t("okay")
            : t("retry")}
        </Button>
      </StepContent>
    </Step>
  );
};
