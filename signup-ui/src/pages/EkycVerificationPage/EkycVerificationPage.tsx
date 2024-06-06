import { useCallback, useEffect, useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import * as yup from "yup";

import { SIGNUP_ROUTE } from "~constants/routes";
import { Form } from "~components/ui/form";
import { getSignInRedirectURL } from "~utils/link";
import { useKycProvidersList } from "~pages/shared/mutations";
import {
  CancelPopup,
  DefaultEkyVerificationProp,
  EkYCVerificationForm,
  SettingsDto,
  UpdateProcessRequestDto,
} from "~typings/types";

import { CancelAlertPopover } from "./CancelAlertPopover";
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

const EkycVerificationFormDefaultValues: EkYCVerificationForm = {
  verifierId: "",
};

const EKYC_VERIFICATION_VALIDATION_SCHEMA = {
  [EkycVerificationStep.VerificationSteps]: yup.object({}),
  [EkycVerificationStep.LoadingScreen]: yup.object({}),
  [EkycVerificationStep.KycProviderList]: yup.object({
    verifierId: yup.string(),
  }),
  [EkycVerificationStep.TermsAndCondition]: yup.object({}),
  [EkycVerificationStep.VideoPreview]: yup.object({}),
  [EkycVerificationStep.SlotCheckingScreen]: yup.object({}),
  [EkycVerificationStep.VerificationScreen]: yup.object({}),
};

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

  const ekycVerificationValidationSchema = useMemo(
    () => Object.values(EKYC_VERIFICATION_VALIDATION_SCHEMA),
    []
  );

  const currentEkycVerificationValidationSchema =
    ekycVerificationValidationSchema[step];

  const methods = useForm<EkYCVerificationForm>({
    shouldUnregister: false,
    defaultValues: EkycVerificationFormDefaultValues,
    resolver: yupResolver(
      currentEkycVerificationValidationSchema
    ) as unknown as Resolver<EkYCVerificationForm, any>,
    mode: "onBlur",
  });

  const { hash: fromSignInHash } = useLocation();

  const hashCode = window.location.hash.substring(1);

  const { kycProvidersList } = useKycProvidersList();

  useEffect(() => {
    if (hashCode !== null && hashCode !== undefined) {
      const decodedBase64 = atob(hashCode);

      const params = new URLSearchParams(decodedBase64);

      const hasState = params.has("state");
      const hasCode = params.has("code");

      if (hasState && hasCode) {
        setHashCode({
          state: params.get("state") ?? "",
          code: params.get("code") ?? "",
        });

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
    window.onbeforeunload = () => {
      return true;
    };

    return () => {
      window.onbeforeunload = null;
    };
  }, [step, criticalError]);

  const cancelAlertPopoverComp = (cancelProp: CancelPopup) => {
    const handleDismiss = () => {
      window.onbeforeunload = null;
      window.location.href = getSignInRedirectURL(
        settings?.response?.configs["signin.redirect-url"],
        fromSignInHash,
        SIGNUP_ROUTE
      );
    };
    return (
      cancelProp.cancelButton && (
        <CancelAlertPopover
          description={"description"}
          handleStay={cancelProp.handleStay}
          handleDismiss={handleDismiss}
        />
      )
    );
  };

  const defaultProps: DefaultEkyVerificationProp = {
    settings: settings?.response,
    methods: methods,
    cancelPopup: cancelAlertPopoverComp,
  };

  const getEkycVerificationStepContent = (step: EkycVerificationStep) => {
    switch (step) {
      case EkycVerificationStep.VerificationSteps:
        return <VerificationSteps {...defaultProps} />;
      case EkycVerificationStep.LoadingScreen:
        return <LoadingScreen />;
      case EkycVerificationStep.KycProviderList:
        return <KycProviderList {...defaultProps} />;
      case EkycVerificationStep.TermsAndCondition:
        return <TermsAndCondition {...defaultProps} />;
      case EkycVerificationStep.VideoPreview:
        return <VideoPreview {...defaultProps} />;
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
