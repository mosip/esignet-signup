import { useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { Form } from "~components/ui/form";
import { EkYCVerificationForm, SettingsDto } from "~typings/types";

import { EkycVerificationPopover } from "./EkycVerificationPopover";
import {
  EkycVerificationStep,
  criticalErrorSelector,
  EkycVerificationStep,
  stepSelector,
  useEkycVerificationStore,
} from "./useEkycVerificationStore";
import VerificationSteps from "./VerificationSteps";
import KycProviderList from "./KycProviderList";
import TermsAndCondition from "./TermsAndCondition";
import VideoPreview from "./VideoPreview";
import VerificationScreen from "./VerificationScreen";

interface EkycVerificationPageProps {
  settings: SettingsDto;
}

const ekycVerificationFormDefaultValues: EkYCVerificationForm = {
  consent: "DECLINED",
  disabilityType: null,
  verifierId: "",
};

const EKYC_VERIFICATION_VALIDATION_SCHEMA = {
  [EkycVerificationStep.VerificationSteps]: yup.object({}),
  [EkycVerificationStep.KycProviderList]: yup.object({}),
  [EkycVerificationStep.TermsAndCondition]: yup.object({}),
  [EkycVerificationStep.VideoPreview]: yup.object({}),
  [EkycVerificationStep.LoadingScreen]: yup.object({}),
  [EkycVerificationStep.VerificationScreen]: yup.object({}),
};

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
