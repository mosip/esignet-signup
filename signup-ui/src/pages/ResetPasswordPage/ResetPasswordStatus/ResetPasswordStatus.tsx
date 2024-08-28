import { useCallback, useEffect } from "react";
import { useMutationState } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { keys as mutationKeys } from "~pages/shared/mutations";
import { useRegistrationStatus } from "~pages/shared/queries";
import {
  RegistrationWithFailedStatus,
  ResetPasswordResponseDto,
  ResetPasswordStatus as ResetPasswordStatusType,
  SettingsDto,
} from "~typings/types";

import {
  ResetPasswordStep,
  setStepSelector,
  useResetPasswordStore,
} from "../useResetPasswordStore";

interface ResetPasswordStatusProps {
  settings: SettingsDto;
}

export const ResetPasswordStatus = ({ settings }: ResetPasswordStatusProps) => {
  const { t } = useTranslation();

  const { setStep } = useResetPasswordStore(
    useCallback((state) => ({ setStep: setStepSelector(state) }), [])
  );

  const [resetPassword] = useMutationState<ResetPasswordResponseDto>({
    filters: { mutationKey: mutationKeys.resetPassword, status: "success" },
    select: (mutation) => mutation.state.data as ResetPasswordResponseDto,
  });

  useEffect(() => {
    // go to the last step on response status is COMPLETED or on error(s)
    if (
      (resetPassword.response &&
        resetPassword.response.status === ResetPasswordStatusType.COMPLETED) ||
      resetPassword.errors.length > 0
    ) {
      setStep(ResetPasswordStep.ResetPasswordConfirmation);
    }
  }, [resetPassword]);

  // isError occurs when the query encounters a network error or the request limit attempts is reached
  const { data: registrationStatus, isError: isRegistrationStatusError } =
    useRegistrationStatus(
      settings.response.configs["status.request.limit"],
      settings.response.configs["status.request.delay"],
      settings.response.configs["status.request.retry.error.codes"].split(","),
      resetPassword
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
      setStep(ResetPasswordStep.ResetPasswordConfirmation);
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
          <Icons.loader className="h-20 w-20 animate-spin text-primary" />
          <div>
            <h1 className="text-center text-2xl font-semibold">
              {t("password_reset_in_progress")}
            </h1>
            <p className="text-center text-muted-neutral-gray">
              {t("password_reset_in_progress_detail")}
            </p>
          </div>
        </div>
      </StepContent>
    </Step>
  );
};
