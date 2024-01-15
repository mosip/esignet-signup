import { useQuery, UseQueryResult } from "@tanstack/react-query";

import {
  RegistrationResponseDto,
  RegistrationStatus,
  RegistrationStatusResponseDto,
  ResetPasswordResponseDto,
  SettingsDto,
} from "~typings/types";

import { getRegistrationStatus, getSettings } from "./service";

export const keys = {
  settings: ["settings"] as const,
  registrationStatus: ["registrationStatus"] as const,
};

export const useSettings = (): UseQueryResult<SettingsDto, unknown> => {
  return useQuery<SettingsDto>({
    queryKey: keys.settings,
    queryFn: () => getSettings(),
    staleTime: Infinity,
  });
};

export const useRegistrationStatus = (
  statusRequestAttempt: number,
  statusRequestDelay: number,
  registration: RegistrationResponseDto | ResetPasswordResponseDto
): UseQueryResult<RegistrationStatusResponseDto, unknown> => {
  return useQuery<RegistrationStatusResponseDto>({
    queryKey: keys.registrationStatus,
    queryFn: () => getRegistrationStatus(),
    gcTime: Infinity,
    retry: statusRequestAttempt - 1, // minus 1 for we called it once already
    retryDelay: statusRequestDelay * 1000,
    enabled:
      !!registration.response &&
      registration.response.status === RegistrationStatus.PENDING,
  });
};
