import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { getSignInRedirectURLV2 } from "~utils/link";
import { useSettings } from "~pages/shared/queries";

export const UploadFileErrorModal = () => {
  const { t } = useTranslation();
  const { data: settings } = useSettings();

  const { hash: fromSignInHash, search } = useLocation();

  const handleContinue = (e: any) => {
    e.preventDefault();
    window.onbeforeunload = null;
    window.location.href = getSignInRedirectURLV2(
      settings?.response.configs["signin.redirect-url"],
      fromSignInHash,
      search,
      "/signup"
    );
  };

  return (
    <Step>
      <StepContent data-testid="slot-unavailable">
        <div className="flex flex-col items-center gap-4 py-4">
          <Icons.failed data-testid="slot-unavailable-failed-icon" />
          <div className="text-center text-lg font-semibold">
            {t("upload_failed.header")}
          </div>
          <p className="text-center text-gray-500">
            {t("upload_failed.description")}
          </p>
        </div>
        <Button
          id="success-continue-button"
          className="my-4 h-16 w-full"
          onClick={handleContinue}
        >
          {t("okay")}
        </Button>
      </StepContent>
    </Step>
  );
};
