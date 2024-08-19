import { useTranslation } from "react-i18next";

import { useL2Hash } from "~hooks/useL2Hash";
import { useIdentityVerificationStatus } from "~pages/shared/queries";
import {
  DefaultEkyVerificationProp,
  IdentityVerificationStatus as IdentityVerificationStatusType,
} from "~typings/types";

import { IdentityVerificationStatusLayout } from "./components/IdentityVerificationStatusLayout";
import { IdentityVerificationStatusLoader } from "./components/IdentityVerificationStatusLoader";

export const IdentityVerificationStatusSuccess = ({
  settings,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation();

  const { state } = useL2Hash();

  const retriableErrorCodes =
    settings.configs["status.request.retry.error.codes"].split(",");

  // isError occurs when the query encounters a network error or the request limit attempts is reached
  const {
    data: identityVerificationStatus,
    isError: isIdentityVerificationStatusError,
  } = useIdentityVerificationStatus({
    attempts: settings.configs["status.request.limit"],
    delay: settings.configs["slot.request.delay"],
    retriableErrorCodes,
  });

  // scenario:
  // - identity verification check status response is `FAILED`
  // - identity verification check status reaches its limit
  //    - UPDATE_PENDING
  //    - error codes specified in `status.request.retry.error.codes`
  if (
    identityVerificationStatus?.response?.status ===
      IdentityVerificationStatusType.FAILED ||
    isIdentityVerificationStatusError
  ) {
    window.onbeforeunload = null;
    window.location.href = `${settings.configs["esignet-consent.redirect-url"]}?key=${state}&error=true`;
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
    window.location.href = `${settings.configs["esignet-consent.redirect-url"]}?key=${state}&error=${identityVerificationStatus.errors[0].errorCode}`;
  }

  // scenario:
  // - identity verification check status response is `COMPLETED`
  if (
    identityVerificationStatus?.response?.status ===
    IdentityVerificationStatusType.COMPLETED
  ) {
    window.onbeforeunload = null;
    window.location.href = `${settings.configs["esignet-consent.redirect-url"]}?key=${state}`;

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
