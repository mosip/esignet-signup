import { ApiService } from "~services/api.service";
import {
  GenerateChallengeRequestDto,
  RegisterRequestDto,
  RegisterStatusResponseDto,
  SettingsDto,
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

export const register = async (register: RegisterRequestDto) => {
  return ApiService.post("/registration/register", register).then(
    ({ data }) => data
  );
};

export const getRegisterStatus =
  async (): Promise<RegisterStatusResponseDto> => {
    return ApiService.get<RegisterStatusResponseDto>(
      "/registration/status"
    ).then(({ data }) => data);
  };
