import { useTranslation } from "react-i18next";

import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";

import { SlotUnavailableAlert } from "./components/SlotUnavailableAlert";

export const SlotChecking = () => {
  const { t } = useTranslation();

  const isFailed = true;

  if (isFailed) return <SlotUnavailableAlert />;

  return (
    <Step>
      <StepContent className="py-16">
        <div className="flex flex-col items-center gap-8">
          <Icons.loader className="h-20 w-20 animate-spin text-primary" />
          <div>
            <h1 className="text-center text-2xl font-semibold">
              {t("slot_checking.header")}
            </h1>
            <p className="text-center text-gray-500">
              {t("slot_checking.description")}
            </p>
          </div>
        </div>
      </StepContent>
    </Step>
  );
};
