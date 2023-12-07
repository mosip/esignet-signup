import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent } from "~components/ui/step";
import { getSignInRedirectURL } from "~utils/link";

export const AccountRegistrationStatus = () => {
  const { t } = useTranslation();
  const { hash: fromSignInHash } = useLocation();

  const handleAction = () => {
    window.location.href = getSignInRedirectURL(fromSignInHash);
  };
  return (
    <Step>
      <StepContent>
        <div className="flex flex-col items-center gap-4 py-4">
          <SuccessIconSvg />
          <div className="text-center text-lg font-semibold">
            <h1>{t("congratulations")}</h1>
            <h2>{t("account_created_successfully")}</h2>
          </div>
          <p className="text-center text-gray-500">{t("login_to_proceed")}</p>
        </div>
        <Button className="my-4 h-16 w-full" onClick={handleAction}>
          {fromSignInHash ? t("login") : t("okay")}
        </Button>
      </StepContent>
    </Step>
  );
};
