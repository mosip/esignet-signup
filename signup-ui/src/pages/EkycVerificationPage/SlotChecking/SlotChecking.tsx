import { useCallback, useEffect } from "react";

import { useSlotAvailability } from "~pages/shared/mutations";
import {
  DefaultEkyVerificationProp,
  SlotAvailabilityRequestDto,
} from "~typings/types";

import {
  criticalErrorSelector,
  EkycVerificationStep,
  kycProviderSelector,
  setCriticalErrorSelector,
  setSlotIdSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { SlotCheckingLoading } from "./components/SlotCheckingLoading";
import { SlotUnavailableAlert } from "./components/SlotUnavailableAlert";

export const SlotChecking = ({ settings }: DefaultEkyVerificationProp) => {
  const { slotAvailabilityMutation } = useSlotAvailability({
    retryAttempt: settings.configs["slot.request.limit"],
    retryDelay: settings.configs["slot.request.delay"],
  });

  const { kycProvider, setStep, setCriticalError, setSlotId, criticalError } =
    useEkycVerificationStore(
      useCallback(
        (state) => ({
          kycProvider: kycProviderSelector(state),
          setStep: setStepSelector(state),
          setCriticalError: setCriticalErrorSelector(state),
          setSlotId: setSlotIdSelector(state),
          criticalError: criticalErrorSelector(state),
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
        consent: "AGREE",
        //disabilityType: "VISION",
      },
    };

    slotAvailabilityMutation.mutate(slotAvailabilityRequestDto, {
      onSuccess: ({ response, errors }) => {
        if (errors.length > 0) {
          setCriticalError(errors[0]);
        } else {
          setSlotId(response.slotId);
          setStep(EkycVerificationStep.VerificationScreen);
        }
      },
    });
  }, []);

  if (slotAvailabilityMutation.isPending) {
    return <SlotCheckingLoading />;
  } else if (
    slotAvailabilityMutation.isSuccess &&
    criticalError?.errorCode === "slot_not_available"
  ) {
    return <SlotUnavailableAlert />;
  }
  return <></>;
};
