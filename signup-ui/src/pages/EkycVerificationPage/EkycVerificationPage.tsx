import { useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

import { Form } from "~components/ui/form";
import { useL2Hash } from "~hooks/useL2Hash";
import { useKycProvidersList } from "~pages/shared/mutations";
import {
  CancelPopup,
  DefaultEkyVerificationProp,
  SettingsDto,
  UpdateProcessRequestDto,
} from "~typings/types";

import { CancelAlertPopover } from "./CancelAlertPopover";
import { EkycVerificationPopover } from "./EkycVerificationPopover";
import IdentityVerificationStatus from "./IdentityVerificationStatus";
import KycProviderList from "./KycProviderList";
import LoadingScreen from "./LoadingScreen";
import SlotChecking from "./SlotChecking";
import TermsAndCondition from "./TermsAndCondition";
import {
  criticalErrorSelector,
  EkycVerificationStep,
  setCriticalErrorSelector,
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
  const navigate = useNavigate();

  const {
    step,
    criticalError,
    setCriticalError,
    setKycProvider,
    setKycProviderList,
    setHashCode,
  } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        criticalError: criticalErrorSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
        setKycProvider: setKycProviderSelector(state),
        setKycProviderList: setKycProvidersListSelector(state),
        setHashCode: setHashCodeSelector(state),
      }),
      []
    )
  );

  const methods = useForm();

  const { state } = useL2Hash();

  const hashCode = window.location.hash.substring(1);

  const { kycProvidersList } = useKycProvidersList();

  const navigateToLandingPage = () => {
    navigate("/");
  };

  useEffect(() => {
    if (!hashCode) return navigateToLandingPage();

    const base64Regex =
      /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;
    if (!base64Regex.test(hashCode)) return navigateToLandingPage();

    const decodedBase64 = atob(hashCode);
    const params = new URLSearchParams(decodedBase64);
    const requiredParams = ["state", "code", "ui_locales"];
    if (!requiredParams.every((param) => params.has(param)))
      return navigateToLandingPage();

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
        if (errors?.length) {
          setCriticalError(errors[0]);
        } else {
          setKycProviderList(response?.identityVerifiers);
          if (response?.identityVerifiers.length === 1) {
            setKycProvider(response?.identityVerifiers[0]);
          }
        }
      },
    });
  }, [hashCode, kycProvidersList, navigate]);

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
      window.location.href = `${settings?.response?.configs["esignet-consent.redirect-url"]}?key=${state}&error=dismiss`;
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
        return <SlotChecking {...defaultProps} />;
      case EkycVerificationStep.VerificationScreen:
        return <VerificationScreen {...defaultProps} />;
      case EkycVerificationStep.IdentityVerificationStatus:
        return <IdentityVerificationStatus {...defaultProps} />;
      default:
        return "unknown step";
    }
  };

  const SCREENS_IN_SESSION_TIMEOUT_SCOPE = [
    EkycVerificationStep.VerificationSteps,
    EkycVerificationStep.LoadingScreen,
    EkycVerificationStep.KycProviderList,
    EkycVerificationStep.TermsAndCondition,
    EkycVerificationStep.VideoPreview,
  ];

  return (
    <>
      {/* TODO: uncomment when needed */}
      {/* {
        <SessionAlert
          isInSessionTimeoutScope={SCREENS_IN_SESSION_TIMEOUT_SCOPE.includes(
            step
          )}
        />
      } */}
      {criticalError &&
        [
          "invalid_transaction",
          "identifier_already_registered",
          "grant_exchange_failed",
        ].includes(criticalError.errorCode) && <EkycVerificationPopover />}

      <Form {...methods}>
        <form noValidate>{getEkycVerificationStepContent(step)}</form>
      </Form>
    </>
  );
};
