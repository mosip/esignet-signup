import { ApiService } from "~services/api.service";
import {
  GenerateChallengeRequestDto,
  KycProvidersResponseDto,
  RegistrationRequestDto,
  RegistrationStatusResponseDto,
  RegistrationWithFailedStatus,
  ResetPasswordRequestDto,
  SettingsDto,
  SlotAvailabilityRequestDto,
  SlotAvailabilityResponseDto,
  TermsAndConditionResponseDto,
  UpdateProcessRequestDto,
  VerifyChallengeRequestDto,
} from "~typings/types";

export const getSettings = async (): Promise<SettingsDto> => {
  return ApiService.get<SettingsDto>("/settings").then(({ data }) => data);
};

export const generateChallenge = async (
  generateChallenge: GenerateChallengeRequestDto
) => {
  return ApiService.post(
    "/registration/generate-challenge",
    generateChallenge
  ).then(({ data }) => data);
};

export const verifyChallenge = async (
  verifyChallenge: VerifyChallengeRequestDto
) => {
  return ApiService.post(
    "/registration/verify-challenge",
    verifyChallenge
  ).then(({ data }) => data);
};

export const register = async (register: RegistrationRequestDto) => {
  return ApiService.post("/registration/register", register).then(
    ({ data }) => data
  );
};

export const getRegistrationStatus =
  async (): Promise<RegistrationStatusResponseDto> => {
    return ApiService.get<RegistrationStatusResponseDto>(
      "/registration/status"
    ).then(({ data }) => {
      // treat PENDING as an error so that react-query will auto retry
      if (data.response?.status === RegistrationWithFailedStatus.PENDING) {
        throw new Error("Status pending");
      }

      return data;
    });
  };

export const resetPassword = async (newUserInfo: ResetPasswordRequestDto) => {
  return ApiService.post("/reset-password", newUserInfo).then(
    ({ data }) => data
  );
};

export const updateProcess = async (updateProcess: UpdateProcessRequestDto) => {
  return ApiService.post("/identity-verification/initiate", updateProcess).then(
    ({ data }) => data
  );
};

export const getTermsAndConditions = async (
  kycProviderId: string
): Promise<TermsAndConditionResponseDto> => {
  return ApiService.get(
    `/identity-verification/identity-verifier/${kycProviderId}`
  ).then(({ data }) => data);
};

export const getKycProvidersList = async (
  updateProcessRequestDto: UpdateProcessRequestDto
): Promise<KycProvidersResponseDto> => {
  return ApiService.post(
    "/identity-verification/initiate",
    updateProcessRequestDto
  ).then(({ data }) => data);
};

export const checkSlotAvailability = async (
  slotAvailabilityRequestDto: SlotAvailabilityRequestDto
) => {
  return ApiService.post<SlotAvailabilityResponseDto>(
    "/identity-verification/slot",
    slotAvailabilityRequestDto
  ).then(({ data }) => {
    if (data.errors.some((error) => error.errorCode === "slot_unavailable")) {
      throw new Error("No slot available");
    }

    return data;
  });
};
