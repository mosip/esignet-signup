import { useQuery, UseQueryResult } from "@tanstack/react-query";

import {
  RegistrationResponseDto,
  RegistrationStatus,
  RegistrationStatusResponseDto,
  ResetPasswordResponseDto,
  SettingsDto,
  TermsAndConditionResponseDto,
} from "~typings/types";

import {
  getKycProvidersList,
  getRegistrationStatus,
  getSettings,
  getTermsAndConditions,
} from "./service";

export const keys = {
  termsAndConditions: ["termsAndConditions"] as const,
  settings: ["settings"] as const,
  registrationStatus: ["registrationStatus"] as const,
  kycProvidersList: ["kycProvidersList"] as const,
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

export const useTermsAndConditions = (kycProviderId: string): UseQueryResult<
  TermsAndConditionResponseDto,
  unknown
> => {
  return useQuery<TermsAndConditionResponseDto>({
    queryKey: [...keys.termsAndConditions, kycProviderId],
    queryFn: () => getTermsAndConditions(kycProviderId),
    staleTime: Infinity,
  });
};
