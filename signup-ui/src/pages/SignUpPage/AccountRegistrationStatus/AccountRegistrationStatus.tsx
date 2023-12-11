import { useMutationState, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { keys as mutationKeys } from "~pages/SignUpPage/mutations";
import { keys as queryKeys } from "~pages/SignUpPage/queries";
import {
  RegistrationResponseDto,
  RegistrationStatus,
  RegistrationStatusResponseDto,
  RegistrationWithFailedStatus,
} from "~typings/types";

import { AccountRegistrationStatusLayout } from "./components/AccountRegistrationStatusLayout";

export const AccountRegistrationStatus = () => {
  const { t } = useTranslation();

  const queryClient = useQueryClient();

  const [registration] = useMutationState<RegistrationResponseDto>({
    filters: { mutationKey: mutationKeys.registration, status: "success" },
    select: (mutation) => mutation.state.data as RegistrationResponseDto,
  });

  const registrationStatus =
    queryClient.getQueryData<RegistrationStatusResponseDto>(
      queryKeys.registrationStatus
    );

  const registrationStatusState = queryClient.getQueryState(
    queryKeys.registrationStatus
  );

  if (!registration) {
    return (
      <AccountRegistrationStatusLayout
        status="failed"
        message={t("something_went_wrong")}
      />
    );
  }

  if (registration && registration.errors.length > 0) {
    return (
      <AccountRegistrationStatusLayout
        status="failed"
        message={t(`error_response.${registration.errors[0].errorCode}`)}
      />
    );
  }

  if (
    registration.response?.status === RegistrationStatus.PENDING &&
    registrationStatusState?.error
  ) {
    return (
      <AccountRegistrationStatusLayout
        status="failed"
        message={t("error_response.request_limit")}
      />
    );
  }

  if (
    registration.response?.status === RegistrationStatus.PENDING &&
    !registrationStatus
  ) {
    return (
      <AccountRegistrationStatusLayout
        status="failed"
        message={t(`error_response.${registration.errors[0].errorCode}`)}
      />
    );
  }

  if (
    registration.response?.status === RegistrationStatus.PENDING &&
    registrationStatus
  ) {
    if (registrationStatus.errors.length > 0) {
      return (
        <AccountRegistrationStatusLayout
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
        <AccountRegistrationStatusLayout
          status="failed"
          message={t("something_went_wrong")}
        />
      );
    }
  }

  return (
    <AccountRegistrationStatusLayout
      status="success"
      message={t("login_to_proceed")}
    />
  );
};
