import { useCallback, useEffect, useState } from "react";
import Stepper from "@keyvaluesystems/react-stepper";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

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
import { checkBrowserCompatible } from "./utils/checkBrowserCompatible";
import { checkBrowserCameraPermission } from "./utils/checkBrowserCameraPermission";

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
  const navigate = useNavigate();
  const { data: settings } = useSettings();

  const hashCode = window.location.hash.substring(1);

  useEffect(() => {
    // if (hashCode !== null && hashCode !== undefined) {
    //   const decodedBase64 = atob(hashCode);

    //   const params = new URLSearchParams(decodedBase64);

    //   const hasState = params.has("state");
    //   const hasCode = params.has("code");

    //   const urlObj = new URL(window.location.href);
    //   const state = urlObj.searchParams.get("state");

    //   if (!hasState && !hasCode) {
    //     const buildRedirectURI = () => {
    //       const authorizeURI =
    //         settings?.response?.configs["signin.redirect-url"];
    //       const clientIdURI =
    //         settings?.response?.configs["signup.oauth-client-id"];
    //       const identityVerificationRedirectURI =
    //         settings?.response?.configs["identity-verification.redirect-url"];

    //       return (
    //         authorizeURI +
    //         "?state=" +
    //         state +
    //         "&client_id=" +
    //         clientIdURI +
    //         "&redirect_uri=" +
    //         identityVerificationRedirectURI +
    //         "&scope=openid&response_type=code&id_token_hint=" +
    //         hashCode
    //       );
    //     };

    //     navigate(buildRedirectURI(), {
    //       replace: true,
    //     });
    //   } else return;
    // }
  }, [settings]);

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

    const browserCompatible = checkBrowserCompatible();
    const permCompatible = await checkBrowserCameraPermission();
    if (browserCompatible && permCompatible) {
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
