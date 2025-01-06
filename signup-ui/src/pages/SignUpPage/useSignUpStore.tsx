import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

import { Error } from "~typings/types";

export enum SignUpStep {
  Phone,
  Otp,
  PhoneStatus,
  AccountSetup,
  AccountSetupStatus,
  AccountRegistrationStatus,
}

const initialState = {
  step: SignUpStep.Phone,
  criticalError: null as Error | null,
  resendOtp: false,
  resendAttempts: null,
  verificationChallengeError: null as Error | null,
};

export type SignUpStore = {
  step: SignUpStep;
  setStep: (step: SignUpStep) => void;
  criticalError: Error | null;
  setCriticalError: (criticalError: Error | null) => void;
  resendOtp: boolean;
  setResendOtp: (resendOtp: boolean) => void;
  resendAttempts: any;
  setResendAttempts: (resendAttempts: any) => void;
  verificationChallengeError: Error | null;
  setVerificationChallengeError: (verificationChallengeError: any) => void;
  reset: () => void;
};

export const useSignUpStore = create<SignUpStore>()(
  devtools((set, get) => ({
    ...initialState,
    setStep: (step: SignUpStep) => {
      const current = get();
      if (isEqual(current.step, step)) return;
      set(() => ({ step }));
    },
    setCriticalError: (criticalError: Error | null) => {
      const current = get();
      if (isEqual(current.criticalError, criticalError)) return;
      set(() => ({ criticalError }));
    },
    setResendOtp: (resendOtp: boolean) => {
      const current = get();
      if (isEqual(current.resendOtp, resendOtp)) return;
      set(() => ({ resendOtp }));
    },
    setResendAttempts: (resendAttempts: any) => {
      const current = get();
      if (isEqual(current.resendAttempts, resendAttempts)) return;
      set(() => ({ resendAttempts }));
    },
    setVerificationChallengeError: (
      verificationChallengeError: Error | null
    ) => {
      const current = get();
      if (
        isEqual(current.verificationChallengeError, verificationChallengeError)
      )
        return;
      set(() => ({ verificationChallengeError }));
    },
    reset: () => set(() => initialState)
  }))
);

export const stepSelector = (state: SignUpStore): SignUpStore["step"] =>
  state.step;

export const setStepSelector = (state: SignUpStore): SignUpStore["setStep"] =>
  state.setStep;

export const resendOtpSelector = (state: SignUpStore): SignUpStore["resendOtp"] =>
  state.resendOtp;

export const setResendOtpSelector = (state: SignUpStore): SignUpStore["setResendOtp"] =>
  state.setResendOtp;

export const resendAttemptsSelector = (state: SignUpStore): SignUpStore["resendAttempts"] =>
  state.resendAttempts;

export const setResendAttemptsSelector = (state: SignUpStore): SignUpStore["setResendAttempts"] =>
  state.setResendAttempts;

export const criticalErrorSelector = (
  state: SignUpStore
): SignUpStore["criticalError"] => state.criticalError;

export const setCriticalErrorSelector = (
  state: SignUpStore
): SignUpStore["setCriticalError"] => state.setCriticalError;

export const verificationChallengeErrorSelector = (
  state: SignUpStore
): SignUpStore["verificationChallengeError"] =>
  state.verificationChallengeError;

export const setVerificationChallengeErrorSelector = (
  state: SignUpStore
): SignUpStore["setVerificationChallengeError"] =>
  state.setVerificationChallengeError;
