import { useCallback } from "react";

import { useIdentityVerificationStatus } from "~pages/shared/queries";
import {
  DefaultEkyVerificationProp,
  IdentityVerificationStatus as IdentityVerificationStatusType,
} from "~typings/types";

import {
  EkycVerificationStep,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { IdentityVerificationStatusLayout } from "./components/IdentityVerificationStatusLayout";
import { IdentityVerificationStatusLayoutPlaceholder } from "./components/IdentityVerificationStatusLayoutPlaceholder";

export const IdentityVerificationStatus = ({
  settings,
}: DefaultEkyVerificationProp) => {
  const { setStep } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
      }),
      []
    )
  );

  // isError occurs when the query encounters a network error or the request limit attempts is reached
  const {
    data: identityVerificationStatus,
    isError: isIdentityVerificationStatusError,
    isLoading,
  } = useIdentityVerificationStatus({
    attempts: settings.configs["status.request.limit"],
    delay: settings.configs["slot.request.delay"],
  });

  const handleIdentityVerificationRetry = () => {
    setStep(EkycVerificationStep.LoadingScreen);
  };

  if (
    identityVerificationStatus?.response?.status ===
    IdentityVerificationStatusType.COMPLETED
  ) {
    return (
      <IdentityVerificationStatusLayout
        status="success"
        title="Verification Successful!"
        description="Please wait while we finalize the process"
      />
    );
  }

  //   identity verification is failed or reaches status check limit
  if (
    identityVerificationStatus?.response?.status ===
      IdentityVerificationStatusType.FAILED ||
    isIdentityVerificationStatusError
  ) {
    return (
      <IdentityVerificationStatusLayout
        status="failed"
        title="Verification Unsuccessful!"
        description="Oops! We were unable to complete the eKYC verification."
        onActionClick={handleIdentityVerificationRetry}
      />
    );
  }

  return <IdentityVerificationStatusLayoutPlaceholder />;
};
