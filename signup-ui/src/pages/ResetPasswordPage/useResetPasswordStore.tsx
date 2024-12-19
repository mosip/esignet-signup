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

const initialState = {
  step: ResetPasswordStep.UserInfo,
  criticalError: null as Error | null,
  resendOtp: false,
  resendAttempts: null,
};

export type ResetPasswordStore = {
  step: ResetPasswordStep;
  setStep: (step: ResetPasswordStep) => void;
  criticalError: Error | null;
  setCriticalError: (criticalError: Error | null) => void;
  resendOtp: boolean;
  setResendOtp: (resendOtp: boolean) => void;
  resendAttempts: any;
  setResendAttempts: (resendAttempts: any) => void;
  reset: () => void;
};

export const useResetPasswordStore = create<ResetPasswordStore>()(
  devtools((set, get) => ({
    ...initialState,
    setStep: (step: ResetPasswordStep) => {
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
    reset: () => set(() => initialState)
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
