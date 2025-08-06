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
import { useSettings } from "~pages/shared/queries";

import {
  hashCodeSelector,
  useEkycVerificationStore,
} from "../../useEkycVerificationStore";

export const UnsupportedBrowserPerm = () => {
  const { t } = useTranslation();
  const { data: settings } = useSettings();

  const { hashCode } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        hashCode: hashCodeSelector(state),
      }),
      []
    )
  );

  const handleOkay = () => {
    window.onbeforeunload = null;
    window.location.href = `${settings?.response?.configs[
      "esignet-consent.redirect-url"
    ]}?key=${hashCode?.state || ""}&error=incompatible_browser`;
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
