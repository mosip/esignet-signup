import { useQuery, UseQueryResult } from "@tanstack/react-query";

import {
  IdentityVerificationStatusResponseDto,
  KycProviderDetailResponseDto,
  RegistrationResponseDto,
  RegistrationStatus,
  RegistrationStatusResponseDto,
  ResetPasswordResponseDto,
  SettingsDto,
} from "~typings/types";

import {
  getIdentityVerificationStatus,
  getRegistrationStatus,
  getSettings,
  getTermsAndConditions,
} from "./service";

export const keys = {
  termsAndConditions: ["termsAndConditions"] as const,
  settings: ["settings"] as const,
  registrationStatus: ["registrationStatus"] as const,
  kycProvidersList: ["kycProvidersList"] as const,
  identityVerificationStatus: ["identityVerificationStatus"] as const,
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
  KycProviderDetailResponseDto,
  unknown
> => {
  return useQuery<KycProviderDetailResponseDto>({
    queryKey: [...keys.termsAndConditions, kycProviderId],
    queryFn: () => getTermsAndConditions(kycProviderId),
    staleTime: Infinity,
  });
};

export const useIdentityVerificationStatus = ({
  attempts: statusRequestAttempt,
  delay: statusRequestDelay,
}: {
  attempts: number;
  delay: number;
}): UseQueryResult<IdentityVerificationStatusResponseDto, unknown> => {
  return useQuery<IdentityVerificationStatusResponseDto>({
    queryKey: keys.identityVerificationStatus,
    queryFn: () => getIdentityVerificationStatus(),
    gcTime: Infinity,
    retry: statusRequestAttempt - 1, // minus 1 for we called it once already
    retryDelay: statusRequestDelay * 1000,
  });
};
