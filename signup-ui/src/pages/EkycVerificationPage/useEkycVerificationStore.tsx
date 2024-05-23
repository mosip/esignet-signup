import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

import { Error } from "~typings/types";

export enum EkycVerificationStep {
  VerificationSteps,
  LoadingScreen,
  KycProviderList,
  TermsAndCondition,
  VideoPreview,
  SlotCheckingScreen,
  VerificationScreen,
}

export type EkycVerificationStore = {
  step: EkycVerificationStep;
  setStep: (step: EkycVerificationStep) => void;
  criticalError: Error | null;
  setCriticalError: (criticalError: Error | null) => void;
  kycProvider: any;
  setKycProvider: (kycProvider: any) => void;
};

// dummy kycprovider data
const dummyKycProvider = {
  id: "Kyc Provider 1",
  logoUrl: "https://avatars.githubusercontent.com/u/39733477?s=200&v=4",
  displayName: {
    en: "Kyc Provider 1",
    km: "Kyc Provider 1 khmer",
    "@none": "Default Kyc Provider 1",
  },
  active: true,
  processType: "VIDEO",
  retryOnFailure: true,
  resumeOnSuccess: true,
};

export const useEkycVerificationStore = create<EkycVerificationStore>()(
  devtools((set, get) => ({
    step: EkycVerificationStep.VerificationSteps,
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
    kycProvider: dummyKycProvider,
    setKycProvider: (kycProvider: any) => {
      const current = get();
      if (isEqual(current.kycProvider, kycProvider)) return;
      set((state) => ({ kycProvider }));
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

export const kycProviderSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["kycProvider"] => state.kycProvider;

export const setKycProviderSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setKycProvider"] => state.setKycProvider;
