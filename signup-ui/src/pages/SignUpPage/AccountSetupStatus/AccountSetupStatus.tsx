import { useCallback, useEffect } from "react";
import { useMutationState } from "@tanstack/react-query";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { keys as mutationKeys } from "~pages/shared/mutations";
import { useRegistrationStatus } from "~pages/shared/queries";
import {
  RegistrationResponseDto,
  RegistrationStatus,
  RegistrationWithFailedStatus,
  SettingsDto,
} from "~typings/types";

import { SignUpForm } from "../SignUpPage";
import { setStepSelector, SignUpStep, useSignUpStore } from "../useSignUpStore";

interface AccountSetupStatusProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountSetupStatus = ({
  methods,
  settings,
}: AccountSetupStatusProps) => {
  const { t } = useTranslation();

  const { setStep } = useSignUpStore(
    useCallback((state) => ({ setStep: setStepSelector(state) }), [])
  );
  const { trigger } = methods;

  const [registration] = useMutationState<RegistrationResponseDto>({
    filters: { mutationKey: mutationKeys.registration, status: "success" },
    select: (mutation) => mutation.state.data as RegistrationResponseDto,
  });

  useEffect(() => {
    // go to the last step on response status is COMPLETED or on error(s)
    if (
      (registration.response &&
        registration.response.status === RegistrationStatus.COMPLETED) ||
      registration.errors.length > 0
    ) {
      setStep(SignUpStep.AccountRegistrationStatus);
    }
  }, [registration]);

  // isError occurs when the query encounters a network error or the request limit attempts is reached
  const { data: registrationStatus, isError: isRegistrationStatusError } =
    useRegistrationStatus(
      settings.response.configs["status.request.limit"],
      settings.response.configs["status.request.delay"],
      settings.response.configs["status.request.retry.error.codes"].split(","),
      registration
    );

  useEffect(() => {
    // go to the last step on registration status FAILED or COMPLETED or reach the request limit
    if (
      (registrationStatus &&
        (registrationStatus.response?.status ===
          RegistrationWithFailedStatus.FAILED ||
          registrationStatus.response?.status ===
            RegistrationWithFailedStatus.COMPLETED ||
          registrationStatus.errors.length > 0)) ||
      isRegistrationStatusError
    ) {
      setStep(SignUpStep.AccountRegistrationStatus);
    }
  }, [
    registrationStatus,
    setStep,
    isRegistrationStatusError,
    settings.response.configs,
  ]);

  return (
    <Step>
      <StepContent className="py-16">
        <div className="flex flex-col items-center gap-8">
          <Icons.loader className="h-20 w-20 animate-spin text-orange-500" />
          <div>
            <h1 className="text-center text-2xl font-semibold">
              {t("setup_progress")}
            </h1>
            <p className="text-center text-gray-500">
              {t("setup_progress_wait")}
            </p>
          </div>
        </div>
      </StepContent>
    </Step>
  );
};
