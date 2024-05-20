import { useCallback, useEffect } from "react";

import { useSlotAvailability } from "~pages/shared/mutations";
import { SlotAvailabilityRequestDto } from "~typings/types";

import {
  setCriticalErrorSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { SlotCheckingLoading } from "./components/SlotCheckingLoading";
import { SlotUnavailableAlert } from "./components/SlotUnavailableAlert";

export const SlotChecking = () => {
  const { slotAvailabilityMutation } = useSlotAvailability();

  const { setCriticalError } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );

  useEffect(() => {
    const slotAvailabilityRequestDto: SlotAvailabilityRequestDto = {
      requestTime: new Date().toISOString(),
      request: {
        verifierId: "12345678",
        consent: "ACCEPTED",
        disabilityType: null,
      },
    };

    slotAvailabilityMutation.mutate(slotAvailabilityRequestDto, {
      onSuccess: ({ errors }) => {
        if (
          errors.length > 0 &&
          errors[0].errorCode === "invalid_transaction"
        ) {
          setCriticalError(errors[0]);
        } else {
          // TODO: Slot Available => Go to Next Step
        }
      },
    });
  }, []);

  if (slotAvailabilityMutation.isPending) {
    return <SlotCheckingLoading />;
  }

  return <SlotUnavailableAlert />;
};
