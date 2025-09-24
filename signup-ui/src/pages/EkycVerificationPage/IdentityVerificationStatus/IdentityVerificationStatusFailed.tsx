import { MouseEventHandler, useCallback } from "react";
import { useTranslation } from "react-i18next";

import { DefaultEkyVerificationProp } from "~typings/types";

import {
  hashCodeSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { IdentityVerificationStatusLayout } from "./components/IdentityVerificationStatusLayout";

export const IdentityVerificationStatusFailed = ({
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

  const handleFailedIdentityVerification: MouseEventHandler<
    HTMLButtonElement
  > = (e) => {
    e.preventDefault();
    window.onbeforeunload = null;
    window.location.href = `${
      settings.configs["esignet-consent.redirect-url"]
    }?key=${hashCode?.state || ""}&error=true`;
  };

  return (
    <IdentityVerificationStatusLayout
      status="failed"
      title={t("identity_verification_status.failed.title")}
      description={t("identity_verification_status.failed.description")}
      btnLabel={t("okay")}
      onBtnClick={handleFailedIdentityVerification}
    />
  );
};
