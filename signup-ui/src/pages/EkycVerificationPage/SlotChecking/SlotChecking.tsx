import { useCallback, useEffect } from "react";

import { useSlotAvailability } from "~pages/shared/mutations";
import {
  DefaultEkyVerificationProp,
  SlotAvailabilityRequestDto,
} from "~typings/types";

import {
  EkycVerificationStep,
  kycProviderSelector,
  setCriticalErrorSelector,
  setStepSelector,
  setSlotIdSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { SlotCheckingLoading } from "./components/SlotCheckingLoading";
import { SlotUnavailableAlert } from "./components/SlotUnavailableAlert";

export const SlotChecking = ({ settings }: DefaultEkyVerificationProp) => {
  const { slotAvailabilityMutation } = useSlotAvailability({
    retryAttempt: settings.configs["slot.request.limit"],
    retryDelay: settings.configs["slot.request.delay"],
  });

  const { kycProvider, setStep, setCriticalError, setSlotId } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        kycProvider: kycProviderSelector(state),
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
        setSlotId: setSlotIdSelector(state)
      }),
      []
    )
  );

  useEffect(() => {
    if (!kycProvider) throw Error("KycProvider should not be null");
    const slotAvailabilityRequestDto: SlotAvailabilityRequestDto = {
      requestTime: new Date().toISOString(),
      request: {
        verifierId: kycProvider?.id ?? "",
        consent: "ACCEPTED",
        //disabilityType: "VISION",
      },
    };

    slotAvailabilityMutation.mutate(slotAvailabilityRequestDto, {
      onSuccess: ({ response, errors }) => {
        if (errors.length > 0) {
          switch (errors[0].errorCode) {
            case "invalid_transaction":
              setCriticalError(errors[0]);
              break;
            case "slot_unavailable":
              break;
          }
        } else {
          setSlotId(response.slotId)
          setStep(EkycVerificationStep.VerificationScreen);
        }
      },
    });
  }, []);

  if (slotAvailabilityMutation.isPending) {
    return <SlotCheckingLoading />;
  }

  return <SlotUnavailableAlert />;
};
