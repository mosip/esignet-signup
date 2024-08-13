import { useCallback, useEffect, useState } from "react";
import Stepper from "@keyvaluesystems/react-stepper";
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
  kycProvidersListSelector,
  setCriticalErrorSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { checkBrowserCompatible } from "./utils/checkBrowserCompatible";

export const VerificationSteps = ({
  cancelPopup,
  settings,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation("translation", {
    keyPrefix: "verification_steps",
  });
  const [cancelButton, setCancelButton] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { setStep, setCriticalError, providerListStore } =
    useEkycVerificationStore(
      useCallback(
        (state: EkycVerificationStore) => ({
          setStep: setStepSelector(state),
          setCriticalError: setCriticalErrorSelector(state),
          providerListStore: kycProvidersListSelector(state),
        }),
        []
      )
    );
  const navigate = useNavigate();
  const base64Regex =
    /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;

  const hashCode = window.location.hash.substring(1);
  var decodedBase64;
  var isValidHash = base64Regex.test(hashCode);

  if (isValidHash) {
    decodedBase64 = atob(hashCode);
  } else {
    navigate("/");
  }

  const params = new URLSearchParams(decodedBase64);
  const hasState = params.has("state");
  const hasCode = params.has("code");
  const uiLocales = params.has("ui_locales");
  const urlObj = new URL(window.location.href);
  const state = urlObj.searchParams.get("state");

  useEffect(() => {
    setIsLoading(true);
    if (hashCode) {
      if (!hasState && !hasCode && uiLocales && isValidHash) {
        const authorizeURI = settings?.configs["signin.redirect-url"];
        const clientIdURI = settings?.configs["signup.oauth-client-id"];
        const identityVerificationRedirectURI =
          settings?.configs["identity-verification.redirect-url"];

        const paramObj = {
          state: state ?? "",
          client_id: clientIdURI ?? "",
          redirect_uri: identityVerificationRedirectURI ?? "",
          scope: "openid",
          response_type: "code",
          id_token_hint: params.get("id_token_hint") ?? "",
          ui_locales: (window as any)._env_.DEFAULT_LANG,
        };

        const redirectParams = new URLSearchParams(paramObj).toString();

        const redirectURI = `${authorizeURI}?${redirectParams}`;

        window.location.replace(redirectURI);
      }
      return;
    }
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
    if (providerListStore && providerListStore.length > 0) {
      setIsLoading(false);
    }
  }, [providerListStore]);

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
                  <Button
                    className="px-[6rem] font-semibold sm:w-full sm:p-4"
                    onClick={handleContinue}
                  >
                    {t("proceed")}
                  </Button>
                </div>
              </StepFooter>
            </Step>
          </div>
        </>
      ) : (
        <LoadingIndicator
          message="please_wait"
          msgParam="Loading. Please wait....."
          iconClass="fill-[#EB6F2D]"
          divClass="align-loading-center"
        />
      )}
    </>
  );
};
