import { useCallback, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { ROOT_ROUTE } from "~constants/routes";
import { useIdentityVerificationStatus } from "~pages/shared/queries";
import {
  DefaultEkyVerificationProp,
  IdentityVerificationStatus as IdentityVerificationStatusType,
} from "~typings/types";

import {
  hashCodeSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { IdentityVerificationStatusLayout } from "./components/IdentityVerificationStatusLayout";
import { IdentityVerificationStatusLoader } from "./components/IdentityVerificationStatusLoader";
import { IdentityVerificationStatusFailed } from "./IdentityVerificationStatusFailed";
import { getStateData } from "~utils/identityVerificationUtil";

export const IdentityVerificationStatus = ({
  settings,
  cancelPopup,
  handleDismiss,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { hash: fromSignInHash } = useLocation();

  const { hashCode } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        hashCode: hashCodeSelector(state),
      }),
      []
    )
  );

  const retriableErrorCodes =
    settings.configs["status.request.retry.error.codes"].split(",");

  const autoRedirectDelay = settings.configs["rp.config"].redirect_delay;

  useEffect(() => {
    if (window.videoLocalStream) {
      window.videoLocalStream.getTracks().forEach((track) => track.stop());
    }
  }, []);

  // isError occurs when the query encounters a network error or the request limit attempts is reached
  const {
    data: identityVerificationStatus,
    isError: isIdentityVerificationStatusError,
  } = useIdentityVerificationStatus({
    attempts: settings.configs["status.request.limit"],
    delay: settings.configs["status.request.delay"],
    retriableErrorCodes,
  });

  // scenario:
  // - identity verification check status response is `FAILED`
  if (
    identityVerificationStatus?.response?.status ===
    IdentityVerificationStatusType.FAILED
  ) {
    return (
      <IdentityVerificationStatusFailed
        settings={settings}
        cancelPopup={cancelPopup}
        handleDismiss={handleDismiss}
      />
    );
  }

  // scenario:
  // - identity verification check status reaches its limit
  //    - UPDATE_PENDING
  //    - error codes specified in `status.request.retry.error.codes`
  if (isIdentityVerificationStatusError) {
    handleDismiss({
      key: hashCode?.state || "",
      error: "ekyc_failed",
    });
  }

  // scenario:
  // - identity verification check returns response with errorCode
  if (
    identityVerificationStatus?.errors &&
    identityVerificationStatus.errors.length > 0 &&
    !retriableErrorCodes.includes(
      identityVerificationStatus.errors[0].errorCode
    )
  ) {
    handleDismiss({
      key: hashCode?.state || "",
      error: identityVerificationStatus.errors[0].errorCode,
    });
  }

  // scenario:
  // - identity verification check status response is `COMPLETED`
  if (
    identityVerificationStatus?.response?.status ===
    IdentityVerificationStatusType.COMPLETED
  ) {
    const handleRedirect = (e?: React.MouseEvent<HTMLButtonElement>) => {
      window.onbeforeunload = null;

      if (getStateData(hashCode?.state || "")) {
        navigate(ROOT_ROUTE);
      } else {
        handleDismiss({ key: hashCode?.state || "" });
      }
    };

    return (
      <IdentityVerificationStatusLayout
        status="success"
        title={t("identity_verification_status.successful.title")}
        description={t("identity_verification_status.successful.description")}
        btnLabel={t("identity_verification_status.successful.button")}
        onBtnClick={handleRedirect}
        autoRedirect={true}
        autoRedirectDelay={autoRedirectDelay}
      />
    );
  }

  return <IdentityVerificationStatusLoader />;
};
