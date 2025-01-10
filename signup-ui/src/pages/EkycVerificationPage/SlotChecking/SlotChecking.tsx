import { useCallback, useEffect } from "react";

import { useSlotAvailability } from "~pages/shared/mutations";
import { ApiError } from "~typings/core";
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
          if (window.videoLocalStream) {
            window.videoLocalStream
              .getTracks()
              .forEach((track) => track.stop());
          }
          setCriticalError(errors[0]);
        } else {
          setSlotId(response.slotId);
          setStep(EkycVerificationStep.VerificationScreen);
        }
      },
      onError: (error: ApiError) => {
        setCriticalError({
          errorCode: error.message as any,
          errorMessage: error.message,
        });
      },
    });
  }, []);

  if (slotAvailabilityMutation.isPending) {
    return <SlotCheckingLoading />;
  } else if (
    criticalError?.errorCode === "slot_not_available"
  ) {
    return <SlotUnavailableAlert />;
  }
  return <></>;
};
