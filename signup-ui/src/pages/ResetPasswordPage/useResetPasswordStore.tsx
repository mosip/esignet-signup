import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

import { Error } from "~typings/types";

export enum ResetPasswordStep {
  UserInfo,
  Otp,
  ResetPassword,
  ResetPasswordStatus,
  ResetPasswordConfirmation,
}

export type ResetPasswordStore = {
  step: ResetPasswordStep;
  setStep: (step: ResetPasswordStep) => void;
  criticalError: Error | null;
  setCriticalError: (criticalError: Error | null) => void;
  resendOtp: boolean;
  setResendOtp: (resendOtp: boolean) => void;
  resendAttempts: any;
  setResendAttempts: (resendAttempts: any) => void;
};

export const useResetPasswordStore = create<ResetPasswordStore>()(
  devtools((set, get) => ({
    step: ResetPasswordStep.UserInfo,
    setStep: (step: ResetPasswordStep) => {
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

export const stepSelector = (
  state: ResetPasswordStore
): ResetPasswordStore["step"] => state.step;

export const setStepSelector = (
  state: ResetPasswordStore
): ResetPasswordStore["setStep"] => state.setStep;

export const criticalErrorSelector = (
  state: ResetPasswordStore
): ResetPasswordStore["criticalError"] => state.criticalError;

export const setCriticalErrorSelector = (
  state: ResetPasswordStore
): ResetPasswordStore["setCriticalError"] => state.setCriticalError;

export const resendOtpSelector = (state: ResetPasswordStore): ResetPasswordStore["resendOtp"] =>
  state.resendOtp;

export const setResendOtpSelector = (state: ResetPasswordStore): ResetPasswordStore["setResendOtp"] =>
  state.setResendOtp;

export const resendAttemptsSelector = (state: ResetPasswordStore): ResetPasswordStore["resendAttempts"] =>
  state.resendAttempts;

export const setResendAttemptsSelector = (state: ResetPasswordStore): ResetPasswordStore["setResendAttempts"] =>
  state.setResendAttempts;
