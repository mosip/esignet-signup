import { useCallback } from "react";
import { useTranslation } from "react-i18next";

import { DefaultEkyVerificationProp } from "~typings/types";

import {
  EkycVerificationStep,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { IdentityVerificationStatusLayout } from "./components/IdentityVerificationStatusLayout";

export const IdentityVerificationStatusFailed = ({
  settings,
  cancelPopup,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation();

  const { setStep } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
      }),
      []
    )
  );

  const handleIdentityVerificationRetry = () => {
    setStep(EkycVerificationStep.LoadingScreen);
  };

  return (
    <IdentityVerificationStatusLayout
      status="failed"
      title={t("identity_verification_status.failed.title")}
      description={t("identity_verification_status.failed.description")}
      btnLabel={t("retry")}
      onBtnClick={handleIdentityVerificationRetry}

    />
  );
};
