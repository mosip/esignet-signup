import { useCallback, useEffect } from "react";
import { useTranslation } from "react-i18next";

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

export const IdentityVerificationStatus = ({
  settings,
  cancelPopup,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation();

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
      />
    );
  }

  // scenario:
  // - identity verification check status reaches its limit
  //    - UPDATE_PENDING
  //    - error codes specified in `status.request.retry.error.codes`
  if (isIdentityVerificationStatusError) {
    window.onbeforeunload = null;
    window.location.href = `${
      settings.configs["esignet-consent.redirect-url"]
    }?key=${hashCode?.state || ""}&error=true`;
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
    window.onbeforeunload = null;
    window.location.href = `${
      settings.configs["esignet-consent.redirect-url"]
    }?key=${hashCode?.state || ""}&error=${
      identityVerificationStatus.errors[0].errorCode
    }`;
  }

  // scenario:
  // - identity verification check status response is `COMPLETED`
  if (
    identityVerificationStatus?.response?.status ===
    IdentityVerificationStatusType.COMPLETED
  ) {
    window.onbeforeunload = null;
    window.location.href = `${
      settings.configs["esignet-consent.redirect-url"]
    }?key=${hashCode?.state || ""}`;

    return (
      <IdentityVerificationStatusLayout
        status="success"
        title={t("identity_verification_status.successful.title")}
        description={t("identity_verification_status.successful.description")}
      />
    );
  }

  return <IdentityVerificationStatusLoader />;
};
