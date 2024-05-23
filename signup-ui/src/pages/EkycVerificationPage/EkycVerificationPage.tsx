import { useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { Form } from "~components/ui/form";
import { useUpdateProcess } from "~pages/shared/mutations";
import { SettingsDto, UpdateProcessRequestDto } from "~typings/types";

import { EkycVerificationPopover } from "./EkycVerificationPopover";
import KycProviderList from "./KycProviderList";
import TermsAndCondition from "./TermsAndCondition";
import {
  criticalErrorSelector,
  EkycVerificationStep,
  stepSelector,
  useEkycVerificationStore,
} from "./useEkycVerificationStore";
import VerificationScreen from "./VerificationScreen";
import VerificationSteps from "./VerificationSteps";
import VideoPreview from "./VideoPreview";
import SlotChecking from "./SlotChecking";

interface EkycVerificationPageProps {
  settings: SettingsDto;
}

export const EkycVerificationPage = ({
  settings,
}: EkycVerificationPageProps) => {
  const { t } = useTranslation();

  const { step, criticalError } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        criticalError: criticalErrorSelector(state),
      }),
      []
    )
  );

  const methods = useForm();

  const hashCode = window.location.hash.substring(1);

  const { updateProcessMutation } = useUpdateProcess();

  useEffect(() => {
    if (hashCode !== null && hashCode !== undefined) {
      const decodedBase64 = atob(hashCode);

      const params = new URLSearchParams(decodedBase64);

      const hasState = params.has("state");
      const hasCode = params.has("code");

      if (hasState && hasCode) {
        if (updateProcessMutation.isPending) return;
        const UpdateProcessRequestDto: UpdateProcessRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            authorizationCode: params?.get("code") ?? "",
            state: params?.get("state") ?? "",
          },
        };
        return updateProcessMutation.mutate(UpdateProcessRequestDto, {
          onSuccess: ({ errors }) => {
            if (errors.length > 0) {
            }

            if (errors.length === 0) {
              return;
            }
          },
          onError: () => {},
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
