import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

export type OtpStore = {
  otpLength?: number;
  setOtpLength: (otpLength: number) => void;
  resendAttempts?: number;
  setResendAttempts: (resendAttempts: number) => void;
  resendDelays?: number;
  setResendDelays: (resendDelays: number) => void;
  resendTimer?: number;
  setResendTimer: (resendTimer: number) => void;
};

export const useOtpStore = create<OtpStore>()(
  devtools((set, get) => ({
    setOtpLength: (otpLength: number) => {
      const current = get();
      if (isEqual(current.otpLength, otpLength)) return;
      set({ otpLength });
    },
    setResendAttempts: (resendAttempts: number) => {
      const current = get();
      if (isEqual(current.resendAttempts, resendAttempts)) return;
      set({ resendAttempts });
    },
    setResendDelays: (resendDelays: number) => {
      const current = get();
      if (isEqual(current.resendDelays, resendDelays)) return;
      set({ resendDelays });
    },
    setResendTimer: (resendTimer: number) => {
      const current = get();
      if (isEqual(current.resendTimer, resendTimer)) return;
      set({ resendTimer });
    },
  }))
);

export const otpLengthSelector = (state: OtpStore): OtpStore["otpLength"] =>
  state.otpLength;

// export const stateSelector = (state: DesignStore): DesignStore["state"] =>
//   state.state;

// export const holdingStateSelector = (
//   state: DesignStore
// ): DesignStore["holdingState"] => state.holdingState;
