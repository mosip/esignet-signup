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

export type SignUpStore = {
  step: SignUpStep;
  setStep: (step: SignUpStep) => void;
  criticalError: Error | null;
  setCriticalError: (criticalError: Error | null) => void;
  resendOtp: boolean;
  setResendOtp: (resendOtp: boolean) => void;
  resendAttempts: any;
  setResendAttempts: (resendAttempts: any) => void;
};

export const useSignUpStore = create<SignUpStore>()(
  devtools((set, get) => ({
    step: SignUpStep.Phone,
    setStep: (step: SignUpStep) => {
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
    resendOtp: false,
    setResendOtp: (resendOtp: boolean) => {
      const current = get();
      if (isEqual(current.resendOtp, resendOtp)) return;
      set((state) => ({ resendOtp }));
    },
    resendAttempts: null,
    setResendAttempts: (resendAttempts: any) => {
      const current = get();
      if (isEqual(current.resendAttempts, resendAttempts)) return;
      set((state) => ({ resendAttempts }));
    },
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
