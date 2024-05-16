import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

import { Error } from "~typings/types";

export enum EkycVerificationStep {
  VerificationSteps,
  KycProviderList,
  TermsAndCondition,
  VideoPreview,
  LoadingScreen,
  VerificationScreen,
}

export type EkycVerificationStore = {
  step: EkycVerificationStep;
  setStep: (step: EkycVerificationStep) => void;
  criticalError: Error | null;
  setCriticalError: (criticalError: Error | null) => void;
};

export const useEkycVerificationStore = create<EkycVerificationStore>()(
  devtools((set, get) => ({
    step: EkycVerificationStep.TermsAndCondition,
    setStep: (step: EkycVerificationStep) => {
      const current = get();
      if (isEqual(current.step, step)) return;
      set((state) => ({ step }));
    },
    criticalError: null,
    setCriticalError: (criticalError: Error | null) => {
      const current = get();
      if (isEqual(current.criticalError, criticalError)) return;
      set((state) => ({ criticalError }));
    },
  }))
);

export const stepSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["step"] => state.step;

export const setStepSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setStep"] => state.setStep;

export const criticalErrorSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["criticalError"] => state.criticalError;

export const setCriticalErrorSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setCriticalError"] => state.setCriticalError;
