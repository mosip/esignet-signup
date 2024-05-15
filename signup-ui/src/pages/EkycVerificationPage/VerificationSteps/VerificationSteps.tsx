import { useCallback, useMemo } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import {
  Step,
  StepContent,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { StepItem, Stepper, Step as StepperStep } from "~components/ui/stepper";
import { cn } from "~utils/cn";

import {
  EkycVerificationStep,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { checkBrowserCameraPermission } from "./utils/checkBrowserCameraPermission";
import { checkBrowserCompatible } from "./utils/checkBrowserCompatible";

interface StepItemWithContent extends StepItem {
  content: string;
}

export const VerificationSteps = () => {
  const { t } = useTranslation();

  const { setStep } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
      }),
      []
    )
  );

  const steps = useMemo(
    () =>
      [
        {
          label: t("verificationSteps.chooseAnEkycProvider.label"),
          content: t("verificationSteps.chooseAnEkycProvider.content"),
        },
        {
          label: t("verificationSteps.termsAndConditions.label"),
          content: t("verificationSteps.termsAndConditions.content"),
        },
        {
          label: t("verificationSteps.preVerificationGuide.label"),
          content: t("verificationSteps.preVerificationGuide.content"),
        },
        {
          label: t("verificationSteps.identityVerification.label"),
          content: t("verificationSteps.identityVerification.content"),
        },
        {
          label: t("verificationSteps.reviewConsent.label"),
          content: t("verificationSteps.reviewConsent.content"),
        },
      ] as StepItemWithContent[],
    [t]
  );

  /**
   * Handle cancel button click, show the cancel alert popover
   * @param e event
   */
  const handleCancel = (e: any) => {
    e.preventDefault();
  };

  /**
   * Handle the proceed button click, move forward to video previe page
   * @param e event
   */
  const handleContinue = async (e: any) => {
    e.preventDefault();

    const browserCompatible = checkBrowserCompatible();
    const permCompatible = await checkBrowserCameraPermission();
    if (browserCompatible && permCompatible) {
      setStep(EkycVerificationStep.LoadingScreen);
    } else {
      setStep(EkycVerificationStep.KycProviderList);
    }
  };

  return (
    <Step className="2xl:max-w-6xl lg:max-w-3xl">
      <StepHeader className="block px-0 sm:px-[18px] sm:pb-[25px] sm:pt-[33px]">
        <StepTitle className="text-left text-[22px] font-semibold">
          {t("verificationSteps.title")}
        </StepTitle>
      </StepHeader>
      <StepDivider />
      <StepContent>
        <div className="flex w-full flex-col gap-4">
          <Stepper
            orientation="vertical"
            initialStep={steps.length}
            steps={steps}
            expandVerticalSteps
            size="lg"
            styles={{
              "step-label": cn("font-semibold"),
            }}
          >
            {steps.map((stepProps, index) => {
              return (
                <StepperStep
                  key={stepProps.label}
                  checkIcon={`${index + 1}`}
                  {...stepProps}
                >
                  <div className="mb-4 ml-2 text-sm">{stepProps.content}</div>
                </StepperStep>
              );
            })}
          </Stepper>
        </div>
      </StepContent>
      <StepDivider />
      <StepFooter className="p-5">
        <div className="w-full">
          <div className="float-right flex w-[50%] flex-row items-center justify-center gap-x-4">
            <Button
              id="cancel-tnc-button"
              name="cancel-tnc-button"
              variant="outline"
              className="w-full border-primary p-4 font-semibold text-primary hover:text-primary"
              onClick={handleCancel}
            >
              {t("cancel_button")}
            </Button>
            <Button
              id="proceed-tnc-button"
              name="proceed-tnc-button"
              className="w-full p-4 font-semibold"
              onClick={handleContinue}
            >
              {t("proceed_button")}
            </Button>
          </div>
        </div>
      </StepFooter>
    </Step>
  );
};
