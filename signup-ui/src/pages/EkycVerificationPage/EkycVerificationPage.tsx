import { useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { Form } from "~components/ui/form";
import { useKycProvidersList } from "~pages/shared/mutations";
import { SettingsDto, UpdateProcessRequestDto } from "~typings/types";

import { EkycVerificationPopover } from "./EkycVerificationPopover";
import KycProviderList from "./KycProviderList";
import LoadingScreen from "./LoadingScreen";
import SlotChecking from "./SlotChecking";
import TermsAndCondition from "./TermsAndCondition";
import {
  criticalErrorSelector,
  EkycVerificationStep,
  setHashCodeSelector,
  setKycProviderSelector,
  setKycProvidersListSelector,
  stepSelector,
  useEkycVerificationStore,
} from "./useEkycVerificationStore";
import VerificationScreen from "./VerificationScreen";
import VerificationSteps from "./VerificationSteps";
import VideoPreview from "./VideoPreview";

interface EkycVerificationPageProps {
  settings: SettingsDto;
}

export const EkycVerificationPage = ({
  settings,
}: EkycVerificationPageProps) => {
  const { t } = useTranslation();

  const {
    step,
    criticalError,
    setKycProvider,
    setKycProviderList,
    setHashCode,
  } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        criticalError: criticalErrorSelector(state),
        setKycProvider: setKycProviderSelector(state),
        setKycProviderList: setKycProvidersListSelector(state),
        setHashCode: setHashCodeSelector(state),
      }),
      []
    )
  );

  const methods = useForm();

  const hashCode = window.location.hash.substring(1);

  const { kycProvidersList } = useKycProvidersList();

  useEffect(() => {
    if (hashCode !== null && hashCode !== undefined) {
      const decodedBase64 = JSON.parse(atob(hashCode));
      setHashCode(decodedBase64);

      const params = new URLSearchParams(decodedBase64);

      const hasState = params.has("state");
      const hasCode = params.has("code");

      if (hasState && hasCode) {
        if (kycProvidersList.isPending) return;
        const UpdateProcessRequestDto: UpdateProcessRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            authorizationCode: params?.get("code") ?? "",
            state: params?.get("state") ?? "",
          },
        };
        return kycProvidersList.mutate(UpdateProcessRequestDto, {
          onSuccess: ({ response, errors }) => {
            if (!errors || errors.length === 0) {
              setKycProviderList(response?.identityVerifiers);
              if (response?.identityVerifiers.length === 1) {
                setKycProvider(response?.identityVerifiers[0]);
              }
              return;
            }
          },
        });
      }
    }
  }, []);

  useEffect(() => {
    const handleTabBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();

      return (event.returnValue = t("reset_password_discontinue_prompt"));
    };

    window.addEventListener("beforeunload", handleTabBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleTabBeforeUnload);
    };
  }, [step, criticalError]);

  const getEkycVerificationStepContent = (step: EkycVerificationStep) => {
    switch (step) {
      case EkycVerificationStep.VerificationSteps:
        return <VerificationSteps />;
      case EkycVerificationStep.LoadingScreen:
        return <LoadingScreen />;
      case EkycVerificationStep.KycProviderList:
        return <KycProviderList />;
      case EkycVerificationStep.TermsAndCondition:
        return <TermsAndCondition />;
      case EkycVerificationStep.VideoPreview:
        return <VideoPreview />;
      case EkycVerificationStep.SlotCheckingScreen:
        return <SlotChecking />;
      case EkycVerificationStep.VerificationScreen:
        return <VerificationScreen />;
      default:
        return "unknown step";
    }
  };

  return (
    <>
      {criticalError &&
        ["invalid_transaction", "identifier_already_registered"].includes(
          criticalError.errorCode
        ) && <EkycVerificationPopover />}

      <Form {...methods}>
        <form noValidate>{getEkycVerificationStepContent(step)}</form>
      </Form>
    </>
  );
};
