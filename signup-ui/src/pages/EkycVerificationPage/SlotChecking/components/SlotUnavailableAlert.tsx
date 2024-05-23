import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";

export const SlotUnavailableAlert = () => {
  const { t } = useTranslation();

  const handleContinue = (e: any) => {
    e.preventDefault();
  };

  return (
    <Step>
      <StepContent>
        <div className="flex flex-col items-center gap-4 py-4">
          <Icons.failed />
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
