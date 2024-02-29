import { useMutationState, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { keys as mutationKeys } from "~pages/shared/mutations";
import { keys as queryKeys } from "~pages/shared/queries";
import {
  RegistrationStatusResponseDto,
  RegistrationWithFailedStatus,
  ResetPasswordResponseDto,
  ResetPasswordStatus,
} from "~typings/types";

import { ResetPasswordConfirmationLayout } from "./components/ResetPasswordConfirmationLayout";

export const ResetPasswordConfirmation = () => {
  const { t } = useTranslation();

  const queryClient = useQueryClient();

  const [resetPassword] = useMutationState<ResetPasswordResponseDto>({
    filters: { mutationKey: mutationKeys.resetPassword, status: "success" },
    select: (mutation) => mutation.state.data as ResetPasswordResponseDto,
  });

  const registrationStatus =
    queryClient.getQueryData<RegistrationStatusResponseDto>(
      queryKeys.registrationStatus
    );

  const registrationStatusState = queryClient.getQueryState(
    queryKeys.registrationStatus
  );

  const { hash: fromSignInHash } = useLocation();

  if (!resetPassword) {
    return (
      <ResetPasswordConfirmationLayout
        status="failed"
        message={t("password_reset_failed_detail")}
      />
    );
  }

  if (resetPassword && resetPassword.errors.length > 0) {
    return (
      <ResetPasswordConfirmationLayout
        status="failed"
        message={t(`error_response.${resetPassword.errors[0].errorCode}`)}
      />
    );
  }

  if (
    resetPassword.response?.status === ResetPasswordStatus.PENDING &&
    registrationStatusState?.error
  ) {
    return (
      <ResetPasswordConfirmationLayout
        status="failed"
        message={t("error_response.reset_pwd_request_limit")}
      />
    );
  }

  if (
    resetPassword.response?.status === ResetPasswordStatus.PENDING &&
    !registrationStatus
  ) {
    return (
      <ResetPasswordConfirmationLayout
        status="failed"
        message={t(`error_response.${resetPassword.errors[0].errorCode}`)}
      />
    );
  }

  if (
    resetPassword.response?.status === ResetPasswordStatus.PENDING &&
    registrationStatus
  ) {
    if (registrationStatus.errors.length > 0) {
      return (
        <ResetPasswordConfirmationLayout
          status="failed"
          message={t(
            `error_response.${registrationStatus.errors[0].errorCode}`
          )}
        />
      );
    }
    if (
      registrationStatus.response &&
      [
        RegistrationWithFailedStatus.FAILED,
        RegistrationWithFailedStatus.PENDING,
      ].includes(registrationStatus.response.status)
    ) {
      return (
        <ResetPasswordConfirmationLayout
          status="failed"
          message={t("password_reset_failed_detail")}
        />
      );
    }
  }

  return (
    <ResetPasswordConfirmationLayout
      status="success"
      message={
        fromSignInHash
          ? t("password_reset_confirmation_detail")
          : t("okay_to_proceed")
      }
    />
  );
};
