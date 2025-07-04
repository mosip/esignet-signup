import { ApiService, AxiosInstance } from "~services/api.service";
import {
  GenerateChallengeRequestDto,
  IdentityVerificationStatus,
  IdentityVerificationStatusResponseDto,
  KycProviderDetailResponseDto,
  KycProvidersResponseDto,
  RegistrationRequestDto,
  RegistrationStatusResponseDto,
  RegistrationWithFailedStatus,
  ResetPasswordRequestDto,
  SettingsDto,
  SlotAvailabilityRequestDto,
  SlotAvailabilityResponseDto,
  UiSchemaResponseDto,
  UpdateProcessRequestDto,
  VerifyChallengeRequestDto,
} from "~typings/types";

/**
 * retrieves cookie from the browser 
 * @param {string} key
 * @returns cookie value
 */
export const getCookie = (key: string): string | any => {
  console.log(document.cookie);
  var b = document.cookie.match("(^|;)\\s*" + key + "\\s*=\\s*([^;]+)");
  console.log(b)
  return b ? b.pop() : "";
}

export const getSettings = async (): Promise<SettingsDto> => {
  return ApiService.get<SettingsDto>("/settings").then(({ data }) => data);
};

export const getCsrfToken = async (): Promise<string> => {
  return AxiosInstance.get("/csrf/token").then(({ data }) => {
    return data.token;
  });
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

export const getRegistrationStatus = async (
  retriableErrorCodes: string[]
): Promise<RegistrationStatusResponseDto> => {
  return ApiService.get<RegistrationStatusResponseDto>(
    "/registration/status"
  ).then(({ data }) => {
    const isErrorRetriable =
      data.errors.length > 0 &&
      retriableErrorCodes.includes(data.errors[0].errorCode);

    const shouldRetryCheckingRegistrationStatus =
      data.response?.status !== RegistrationWithFailedStatus.COMPLETED &&
      data.response?.status !== RegistrationWithFailedStatus.FAILED &&
      (data.response?.status === RegistrationWithFailedStatus.PENDING ||
        isErrorRetriable);

    if (shouldRetryCheckingRegistrationStatus) {
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
): Promise<KycProviderDetailResponseDto> => {
  return ApiService.get(
    `/identity-verification/identity-verifier/${kycProviderId}`
  ).then(({ data }) => data);
};

export const getKycProvidersList = async (
  updateProcessRequestDto: UpdateProcessRequestDto
): Promise<KycProvidersResponseDto> => {
  return ApiService.post(
    "/identity-verification/initiate",
    updateProcessRequestDto,
    {
      headers:{
        "X-XSRF-TOKEN": getCookie('XSRF-TOKEN'),
      }
    }
  ).then(({ data }) => data);
};

export const checkSlotAvailability = async (
  slotAvailabilityRequestDto: SlotAvailabilityRequestDto
) => {
  return ApiService.post<SlotAvailabilityResponseDto>(
    "/identity-verification/slot",
    slotAvailabilityRequestDto
  ).then(({ data }) => {
    if (data.errors.some((error) => error.errorCode === "slot_not_available")) {
      throw new Error("slot_not_available");
    }

    return data;
  });
};

export const getIdentityVerificationStatus = async (
  retriableErrorCodes: string[]
): Promise<IdentityVerificationStatusResponseDto> => {
  return ApiService.get<IdentityVerificationStatusResponseDto>(
    "/identity-verification/status"
  ).then(({ data }) => {
    const isErrorRetriable =
      data.errors.length > 0 &&
      retriableErrorCodes.includes(data.errors[0].errorCode);

    const shouldRetryCheckingIdentityVerificationStatus =
      (data.response?.status !== IdentityVerificationStatus.COMPLETED &&
        data.response?.status !== IdentityVerificationStatus.FAILED) ||
      isErrorRetriable;

    console.log(shouldRetryCheckingIdentityVerificationStatus);

    if (shouldRetryCheckingIdentityVerificationStatus) {
      throw new Error("Identity verification update is pending");
    }

    return data;
  });
};

export const getUiSpec = async (): Promise<UiSchemaResponseDto> => {
  return ApiService.get<UiSchemaResponseDto>("/registration/ui-spec").then(
    ({ data }) => data
  );
};
