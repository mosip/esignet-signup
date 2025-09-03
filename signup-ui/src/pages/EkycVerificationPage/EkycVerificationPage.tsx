import { useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";

import { Form } from "~components/ui/form";
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
  setProviderListStatusSelector,
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
  const [searchParams, setSearchParams] = useSearchParams();

  const {
    step,
    criticalError,
    setCriticalError,
    setKycProvider,
    setKycProviderList,
    setHashCode,
    setProviderListStatus,
  } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        criticalError: criticalErrorSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
        setKycProvider: setKycProviderSelector(state),
        setKycProviderList: setKycProvidersListSelector(state),
        setHashCode: setHashCodeSelector(state),
        setProviderListStatus: setProviderListStatusSelector(state),
      }),
      []
    )
  );

  const methods = useForm();

  const { kycProvidersList } = useKycProvidersList();

  const navigateToLandingPage = () => {
    navigate("/");
  };

  useEffect(() => {
    const hasRequiredParams =
      searchParams.has("state") &&
      searchParams.has("code") &&
      searchParams.has("ui_locales");

    if (hasRequiredParams) {
      setHashCode({
        state: searchParams.get("state") ?? "",
        code: searchParams.get("code") ?? "",
        uiLocales: searchParams.get("ui_locales") ?? ""
      });

      if (kycProvidersList.isPending) return;
      const UpdateProcessRequestDto: UpdateProcessRequestDto = {
        requestTime: new Date().toISOString(),
        request: {
          authorizationCode: searchParams?.get("code") ?? "",
          state: searchParams?.get("state") ?? "",
        },
      };
      return kycProvidersList.mutate(UpdateProcessRequestDto, {
        onSuccess: ({ response, errors }) => {
          if (errors?.length) {
            setCriticalError(errors[0]);
          } else {
            setKycProviderList(response?.identityVerifiers);
            setProviderListStatus(true);
            if (response?.identityVerifiers.length === 1) {
              setKycProvider(response?.identityVerifiers[0]);
            }
          }
        },
      });
    } else if (searchParams.has("id_token_hint")) {
      const authorizeURI = settings?.response?.configs["signin.redirect-url"];
      const clientIdURI = settings?.response?.configs["signup.oauth-client-id"];
      const identityVerificationRedirectURI =
        settings?.response?.configs["identity-verification.redirect-url"];
      const urlObj = new URL(window.location.href);
      const state = urlObj.searchParams.get("state");

      const paramObj = {
        state: state ?? "",
        client_id: clientIdURI ?? "",
        redirect_uri: identityVerificationRedirectURI ?? "",
        scope: "openid",
        response_type: "code",
        id_token_hint: searchParams.get("id_token_hint") ?? "",
        ui_locales: (window as any)._env_.DEFAULT_LANG,
      };

      const redirectParams = new URLSearchParams(paramObj).toString();

      const redirectURI = `${authorizeURI}?${redirectParams}`;

      window.location.replace(redirectURI);
    } else {
      navigateToLandingPage();
    }
  }, [settings]);

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
      window.location.href = `${settings?.response?.configs[
        "esignet-consent.redirect-url"
      ]}?key=${searchParams.get("state") || ""}&error=dismiss`;
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
