import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { useESignetRedirect } from "~hooks/useESignetRedirect";

export const SlotUnavailableAlert = () => {
  const { t } = useTranslation();

  const { handleRedirectToSignIn: handleContinue } = useESignetRedirect();

  return (
    <Step>
      <StepContent data-testid="slot-unavailable">
        <div className="flex flex-col items-center gap-4 py-4">
          <Icons.failed data-testid="slot-unavailable-failed-icon" />
          <div className="text-center text-lg font-semibold">
            {t("slot_unavailable.header")}
          </div>
          <p className="text-center text-gray-500">
            {t("slot_unavailable.description")}
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
