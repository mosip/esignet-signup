import { useCallback } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import {
  Step,
  StepContent,
  StepDescription,
  StepHeader,
  StepTitle,
} from "~components/ui/step";

import {
  hashCodeSelector,
  useEkycVerificationStore,
} from "../../useEkycVerificationStore";

export const UnsupportedBrowserPerm = ({
  handleDismiss,
}: {
  handleDismiss: (args: { key: string; error: string }) => void;
}) => {
  const { t } = useTranslation();

  const { hashCode } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        hashCode: hashCodeSelector(state),
      }),
      []
    )
  );

  const handleOkay = () => {
    handleDismiss({
      key: hashCode?.state || "",
      error: "incompatible_browser",
    });
  };

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
          onClick={handleOkay}
          type="button"
        >
          {t("okay")}
        </Button>
      </StepContent>
    </Step>
  );
};
