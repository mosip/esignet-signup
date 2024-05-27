import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

import { Error, KycProvider, SignupHashCode } from "~typings/types";

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
  kycProvider: KycProvider | null;
  setKycProvider: (kycProvider: KycProvider) => void;
  kycProvidersList: KycProvider[] | null;
  setKycProvidersList: (kycProvidersList: KycProvider[]) => void;
  hashCode: SignupHashCode | null;
  setHashCode: (hashCode: SignupHashCode) => void;
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
    kycProvider: null,
    setKycProvider: (kycProvider: KycProvider) => {
      const current = get();
      if (isEqual(current.kycProvider, kycProvider)) return;
      set((state) => ({ kycProvider }));
    },
    kycProvidersList: [],
    setKycProvidersList: (kycProvidersList: KycProvider[] | null) => {
      const current = get();
      if (isEqual(current.kycProvidersList, kycProvidersList)) return;
      set((state) => ({ kycProvidersList }));
    },
    hashCode: null,
    setHashCode: (hashCode: SignupHashCode | null) => {
      const current = get();
      if (isEqual(current.hashCode, hashCode)) return;
      set((state) => ({ hashCode }));
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

export const kycProvidersListSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["kycProvidersList"] => state.kycProvidersList;

export const setKycProvidersListSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setKycProvidersList"] => state.setKycProvidersList;

export const hashCodeSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["hashCode"] => state.hashCode;

export const setHashCodeSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setHashCode"] => state.setHashCode;
