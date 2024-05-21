import { useCallback, useEffect, useState } from "react";
import Stepper from "@keyvaluesystems/react-stepper";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { SIGNUP_ROUTE } from "~constants/routes";
import { Button } from "~components/ui/button";
import {
  Step,
  StepContent,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { getSignInRedirectURL } from "~utils/link";
import { useSettings } from "~pages/shared/queries";

import { CancelAlertPopover } from "../CancelAlertPopover";
import {
  EkycVerificationStep,
  EkycVerificationStore,
  setCriticalErrorSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";

export const VerificationSteps = () => {
  const { t } = useTranslation("translation", {
    keyPrefix: "verification_steps",
  });
  const [cancelButton, setCancelButton] = useState<boolean>(false);

  const { setStep, setCriticalError } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );

  const { hash: fromSignInHash } = useLocation();
  const { data: settings } = useSettings();

  const eKYCSteps = [
    {
      stepLabel: t("Choose an eKYC provider"),
      stepDescription: t(
        "Select an eKYC service provider that aligns with your requirements"
      ),
      isCompleted: false,
    },
    {
      stepLabel: t("Terms & Conditions"),
      stepDescription: t("Review the policy terms & conditions"),
      isCompleted: false,
    },
    {
      stepLabel: t("Pre-verification guide"),
      stepDescription: t("Key instructions for a seamless eKYC experience"),
      isCompleted: false,
    },
    {
      stepLabel: t("Identity verification"),
      stepDescription:
        t("This step verifies the individual’s physical presence during the identity verification process as well as verification of the individual’s identity with their physical ID"),
      isCompleted: false,
    },
    {
      stepLabel: t("Review Consent"),
      stepDescription:
        t("Review and approve consent before sharing with the service provider"),
      isCompleted: false,
    },
  ];

  const handleContinue = (e: any) => {
    e.preventDefault();
    setStep(EkycVerificationStep.KycProviderList);
  };

  const handleCancel = (e: any) => {
    e.preventDefault();
    setCancelButton(true);
  };

  const handleStay = () => {
    setCancelButton(false);
  };

  const handleDismiss = () => {
    window.location.href = getSignInRedirectURL(
      settings?.response.configs["signin.redirect-url"],
      fromSignInHash,
      SIGNUP_ROUTE
    );
  };

  useEffect(() => {}, [setStep]);

  return (
    <>
      {cancelButton && (
        <CancelAlertPopover
          description={"description"}
          handleStay={handleStay}
          handleDismiss={handleDismiss}
        />
      )}
      <div className="m-3 flex flex-row justify-center">
      <Step className="max-w-[75rem] md:rounded-2xl md:shadow sm:rounded-2xl sm:shadow sm:mt-0 my-5">
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
              className="max-w-max px-[6rem] font-semibold sm:px-[3rem] xs:px-[2rem]"
              onClick={handleCancel}
            >
              {t("cancel")}
            </Button>
            <Button
              className="max-w-max px-[6rem] font-semibold sm:px-[3rem] xs:px-[2rem]"
              onClick={handleContinue}
            >
              {t("proceed")}
            </Button>
          </div>
        </StepFooter>
      </Step>
      </div>
    </>
  );
};
