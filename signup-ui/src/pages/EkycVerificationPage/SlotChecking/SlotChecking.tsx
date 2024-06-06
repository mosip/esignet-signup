import { useCallback, useEffect } from "react";
import { useFormContext } from "react-hook-form";

import { useSlotAvailability } from "~pages/shared/mutations";
import { SlotAvailabilityRequestDto } from "~typings/types";

import {
  EkycVerificationStep,
  setCriticalErrorSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { SlotCheckingLoading } from "./components/SlotCheckingLoading";
import { SlotUnavailableAlert } from "./components/SlotUnavailableAlert";

export const SlotChecking = () => {
  const { slotAvailabilityMutation } = useSlotAvailability();

  const { getValues } = useFormContext();

  const { setStep, setCriticalError } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );

  useEffect(() => {
    const slotAvailabilityRequestDto: SlotAvailabilityRequestDto = {
      requestTime: new Date().toISOString(),
      request: {
        verifierId: getValues("verifierId"),
        consent: "ACCEPTED",
        disabilityType: null,
      },
    };

    slotAvailabilityMutation.mutate(slotAvailabilityRequestDto, {
      onSuccess: ({ errors }) => {
        if (errors.length > 0) {
          switch (errors[0].errorCode) {
            case "invalid_transaction":
              setCriticalError(errors[0]);
              break;
            case "slot_unavailable":
              break;
          }
        } else {
          setStep(EkycVerificationStep.VerificationScreen)
        }
      },
    });
  }, []);

  if (slotAvailabilityMutation.isPending) {
    return <SlotCheckingLoading />;
  }

  return <SlotUnavailableAlert />;
};
