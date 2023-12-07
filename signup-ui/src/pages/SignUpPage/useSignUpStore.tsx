import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

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
};

export const useSignUpStore = create<SignUpStore>()(
  devtools((set, get) => ({
    step: SignUpStep.Phone,
    setStep: (step: SignUpStep) => {
      const current = get();
      if (isEqual(current.step, step)) return;
      set((state) => ({ step }));
    },
  }))
);

export const stepSelector = (state: SignUpStore): SignUpStore["step"] =>
  state.step;

export const setStepSelector = (state: SignUpStore): SignUpStore["setStep"] =>
  state.setStep;
