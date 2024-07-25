import { useTranslation } from "react-i18next";

import { useL2Hash } from "~hooks/useL2Hash";
import { useIdentityVerificationStatus } from "~pages/shared/queries";
import {
  DefaultEkyVerificationProp,
  IdentityVerificationStatus as IdentityVerificationStatusType,
} from "~typings/types";

import { IdentityVerificationStatusLayout } from "./components/IdentityVerificationStatusLayout";

export const IdentityVerificationStatusSuccess = ({
  settings,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation();

  const { state } = useL2Hash();

  // isError occurs when the query encounters a network error or the request limit attempts is reached
  const {
    data: identityVerificationStatus,
    isError: isIdentityVerificationStatusError,
    isLoading: isCheckingIdentityVerificationStatus,
  } = useIdentityVerificationStatus({
    attempts: settings.configs["status.request.limit"],
    delay: settings.configs["slot.request.delay"],
  });

  if (
    identityVerificationStatus?.response?.status ===
    IdentityVerificationStatusType.COMPLETED
  ) {
    window.onbeforeunload = null;
    window.location.href = `${settings.configs["esignet-consent.redirect-url"]}?key=${state}`;
  }

  if (
    identityVerificationStatus?.response?.status ===
      IdentityVerificationStatusType.FAILED ||
    isIdentityVerificationStatusError
  ) {
    // TODO: handle scenario when identity verification fails or reaches its limit
  }

  return (
    <IdentityVerificationStatusLayout
      status="success"
      title={t("identity_verification_status.successful.title")}
      description={
        isCheckingIdentityVerificationStatus
          ? t("identity_verification_status.successful.description")
          : "FAILED..."
      }
    />
  );
};
