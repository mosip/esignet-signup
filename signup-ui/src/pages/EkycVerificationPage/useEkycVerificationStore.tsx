import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

import {
  Error,
  KycProvider,
  KycProviderDetail,
  SignupHashCode,
} from "~typings/types";

export enum EkycVerificationStep {
  VerificationSteps,
  LoadingScreen,
  KycProviderList,
  TermsAndCondition,
  VideoPreview,
  SlotCheckingScreen,
  VerificationScreen,
  IdentityVerificationStatus,
}

export type EkycVerificationStore = {
  step: EkycVerificationStep;
  setStep: (step: EkycVerificationStep) => void;
  criticalError: Error | null;
  setCriticalError: (criticalError: Error | null) => void;
  kycProvider: KycProvider | null;
  setKycProvider: (kycProvider: KycProvider) => void;
  kycProviderDetail: KycProviderDetail | null;
  setKycProviderDetail: (kycProviderDetail: KycProviderDetail) => void;
  kycProvidersList: KycProvider[] | null;
  setKycProvidersList: (kycProvidersList: KycProvider[]) => void;
  hashCode: SignupHashCode | null;
  setHashCode: (hashCode: SignupHashCode) => void;
  isNoBackground: boolean;
  setIsNoBackground: (isNoBackground: boolean) => void;
  errorBannerMessage: string | null;
  setErrorBannerMessage: (errorBannerMessage: string | null) => void;
  slotId: string | null;
  setSlotId: (slotId: string | null) => void;
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
    kycProviderDetail: null,
    setKycProviderDetail: (kycProviderDetail: KycProviderDetail) => {
      const current = get();
      if (isEqual(current.kycProviderDetail, kycProviderDetail)) return;
      set((state) => ({ kycProviderDetail }));
    },
    kycProvidersList: null,
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
    isNoBackground: false,
    setIsNoBackground: (isNoBackground: boolean) => {
      const current = get();
      if (isEqual(current.isNoBackground, isNoBackground)) return;
      set((state) => ({ isNoBackground }));
    },
    errorBannerMessage: null,
    setErrorBannerMessage: (errorBannerMessage: string | null) => {
      const current = get();
      if (isEqual(current.errorBannerMessage, errorBannerMessage)) return;
      set((state) => ({ errorBannerMessage }));
    },
    slotId: null,
    setSlotId: (slotId: string | null) => {
      const current = get();
      if (isEqual(current.slotId, slotId)) return;
      set((state) => ({ slotId }));
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

export const kycProviderDetailSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["kycProviderDetail"] => state.kycProviderDetail;

export const setKycProviderDetailSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setKycProviderDetail"] => state.setKycProviderDetail;

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

export const isNoBackgroundSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["isNoBackground"] => state.isNoBackground;

export const setIsNoBackgroundSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setIsNoBackground"] => state.setIsNoBackground;

export const errorBannerMessageSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["errorBannerMessage"] => state.errorBannerMessage;

export const setErrorBannerMessageSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setErrorBannerMessage"] =>
  state.setErrorBannerMessage;

export const slotIdSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["slotId"] => state.slotId;

export const setSlotIdSelector = (
  state: EkycVerificationStore
): EkycVerificationStore["setSlotId"] => state.setSlotId;
