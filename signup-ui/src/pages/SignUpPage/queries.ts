import { useQuery, UseQueryResult } from "@tanstack/react-query";

import { RegistrationStatusResponseDto, SettingsDto } from "~typings/types";

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
  statusRequestDelay: number
): UseQueryResult<RegistrationStatusResponseDto, unknown> => {
  return useQuery<RegistrationStatusResponseDto>({
    queryKey: keys.registrationStatus,
    queryFn: () => getRegistrationStatus(),
    retry: statusRequestAttempt,
    retryDelay: statusRequestDelay * 1000,
  });
};
