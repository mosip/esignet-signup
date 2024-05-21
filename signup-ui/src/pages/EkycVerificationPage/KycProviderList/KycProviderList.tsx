import { useCallback, useEffect } from "react";

import { Button } from "~components/ui/button";

import {
  EkycVerificationStep,
  EkycVerificationStore,
  setCriticalErrorSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";

export const KycProviderList = () => {

  const { setStep, setCriticalError } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );

  const handleContinue = () => {
    setStep(EkycVerificationStep.TermsAndCondition)
  }

  useEffect(() => {}, [setStep]);

  return (
    <>
      <h1>Kyc Provider List</h1>
      <Button onClick={handleContinue}>Proceed</Button>
    </>
  );
};
