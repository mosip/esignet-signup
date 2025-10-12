import { useCallback } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { useSettings } from "~pages/shared/queries";

import { criticalErrorSelector, useSignUpStore } from "../../useSignUpStore";

export const UploadFileErrorModal = () => {
  const { t } = useTranslation();
  const { data: settings } = useSettings();

  const { criticalError } = useSignUpStore(
    useCallback(
      (state) => ({
        criticalError: criticalErrorSelector(state),
      }),
      []
    )
  );

  const handleContinue = (e: any) => {
    e.preventDefault();
    window.onbeforeunload = null;
    window.location.href = `${settings?.response?.configs["esignet-consent.redirect-url"]}?&error=${criticalError?.errorCode}`;
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
