import { useCallback, useEffect, useState } from "react";
import Stepper from "@keyvaluesystems/react-stepper";
import { Detector, PollingConfig } from "react-detect-offline";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

import { Button } from "~components/ui/button";
import {
  Step,
  StepContent,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { DefaultEkyVerificationProp } from "~typings/types";
import LoadingIndicator from "~/common/LoadingIndicator";

import {
  EkycVerificationStep,
  EkycVerificationStore,
  hashCodeSelector,
  setStepSelector,
  providerListStatusSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { checkBrowserCompatible } from "./utils/checkBrowserCompatible";

const POLLING_BASE_URL =
  process.env.NODE_ENV === "development"
    ? process.env.REACT_APP_API_BASE_URL
    : window.origin + "/v1/signup";

export const VerificationSteps = ({
  cancelPopup,
  settings,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation("translation", {
    keyPrefix: "verification_steps",
  });
  const [cancelButton, setCancelButton] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const { setStep, providerListStatus, hashCode } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        setStep: setStepSelector(state),
        providerListStatus: providerListStatusSelector(state),
        hashCode: hashCodeSelector(state),
      }),
      []
    )
  );

  const pollingUrl = POLLING_BASE_URL + "/actuator/health";
  const pollingConfig: PollingConfig = {
    timeout: settings?.configs?.["offline.polling.timeout"] ?? 5000,
    interval: settings?.configs?.["offline.polling.interval"] ?? 10000,
    enabled: settings?.configs?.["offline.polling.enabled"] ?? true,
    url: settings?.configs?.["offline.polling.url"] ?? pollingUrl,
  };

  const hasState = hashCode?.state && hashCode.state !== "";
  const hasCode = hashCode?.code && hashCode.code !== "";
  const uiLocales = hashCode?.uiLocales && hashCode.uiLocales !== "";

  const eKYCSteps = [
    {
      stepLabel: t("ekycSteps.ekyc_provider.label"),
      stepDescription: t("ekycSteps.ekyc_provider.description"),
      isCompleted: false,
    },
    {
      stepLabel: t("ekycSteps.terms_&_conditions.label"),
      stepDescription: t("ekycSteps.terms_&_conditions.description"),
      isCompleted: false,
    },
    {
      stepLabel: t("ekycSteps.pre_verification_guide.label"),
      stepDescription: t("ekycSteps.pre_verification_guide.description"),
      isCompleted: false,
    },
    {
      stepLabel: t("ekycSteps.identity_verification.label"),
      stepDescription: t("ekycSteps.identity_verification.description"),
      isCompleted: false,
    },
    {
      stepLabel: t("ekycSteps.review_consent.label"),
      stepDescription: t("ekycSteps.review_consent.description"),
      isCompleted: false,
    },
  ];

  const handleContinue = async (e: any) => {
    e.preventDefault();

    // Check if the browser is compatible
    // with the minimum version from settings config
    const browserCompatible = checkBrowserCompatible(
      settings.configs["broswer.minimum-version"]
    );

    // Removed camera permission check
    // const permCompatible = true;
    // await checkBrowserCameraPermission();
    if (browserCompatible) {
      setStep(EkycVerificationStep.KycProviderList);
    } else {
      setStep(EkycVerificationStep.LoadingScreen);
    }
  };

  const handleCancel = (e: any) => {
    e.preventDefault();
    setCancelButton(true);
  };

  const handleStay = () => {
    setCancelButton(false);
  };

  useEffect(() => {
    if (providerListStatus) {
      setIsLoading(false);
    }
  }, [providerListStatus]);

  return (
    <>
      {hasState && hasCode && uiLocales && !isLoading ? (
        <>
          {cancelPopup({ cancelButton, handleStay })}
          <div className="my-4 flex flex-row justify-center">
            <Step className="my-5 max-w-[75rem] md:rounded-2xl md:shadow sm:mt-0 sm:rounded-2xl sm:shadow">
              <StepHeader className="px-0 py-5 sm:py-[25px]">
                <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-base font-semibold">
                  <div
                    className="ml-5 w-full text-[22px] font-semibold"
                    id="tnc-header"
                  >
                    {t("header")}
                  </div>
                </StepTitle>
              </StepHeader>
              <StepDivider />
              <StepContent className="px-5 py-0">
                <Stepper
                  steps={eKYCSteps}
                  labelPosition="right"
                  showDescriptionsForAllSteps
                />
              </StepContent>
              <StepDivider />
              <StepFooter className="p-5">
                <div className="flex w-full flex-row items-center justify-end gap-x-4">
                  <Button
                    variant="cancel_outline"
                    className="px-[6rem] font-semibold sm:w-full sm:p-4"
                    onClick={handleCancel}
                  >
                    {t("cancel")}
                  </Button>
                  <Detector
                    polling={pollingConfig}
                    render={({ online }) => (
                      <Button
                        className="px-[6rem] font-semibold sm:w-full sm:p-4"
                        onClick={handleContinue}
                        disabled={!online}
                      >
                        {t("proceed")}
                      </Button>
                    )}
                  />
                </div>
              </StepFooter>
            </Step>
          </div>
        </>
      ) : (
        <LoadingIndicator
          message="please_wait"
          msgParam="Loading. Please wait....."
          iconClass="loading-indicator"
          divClass="align-loading-center"
        />
      )}
    </>
  );
};
