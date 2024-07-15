import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import {
  Step,
  StepContent,
  StepDescription,
  StepHeader,
  StepTitle,
} from "~components/ui/step";

export const UnsupportedBrowserPerm = () => {
  const { t } = useTranslation();

  return (
    <Step>
      <StepHeader>
        <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-[26px]">
          {t("eykc_loading.header")}
        </StepTitle>
        <StepDescription>{t("eykc_loading.description")}</StepDescription>
      </StepHeader>
      <StepContent>
        <Button
          id="okay-button"
          name="okay-button"
          className="my-4 h-16 w-full"
        >
          {t("okay")}
        </Button>
      </StepContent>
    </Step>
  );
};
