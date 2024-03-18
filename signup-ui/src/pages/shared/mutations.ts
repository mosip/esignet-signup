import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

import { ApiError } from "~typings/core";
import {
  GenerateChallengeRequestDto,
  GenerateChallengeResponseDto,
  RegistrationRequestDto,
  RegistrationResponseDto,
  ResetPasswordRequestDto,
  ResetPasswordResponseDto,
  VerifyChallengeRequestDto,
  VerifyChallengeResponseDto,
} from "~typings/types";

import {
  generateChallenge,
  register,
  resetPassword,
  verifyChallenge,
} from "./service";

export const keys = {
  challengeGeneration: ["challengeGeneration"] as const,
  challengeVerification: ["challengeVerification"] as const,
  registration: ["registration"] as const,
  resetPassword: ["resetPassword"] as const,
};

export const useGenerateChallenge = () => {
  const generateChallengeMutation = useMutation<
    GenerateChallengeResponseDto,
    ApiError,
    GenerateChallengeRequestDto
  >({
    mutationKey: keys.challengeGeneration,
    mutationFn: (generateChallengeRequestDto: GenerateChallengeRequestDto) =>
      generateChallenge(generateChallengeRequestDto),
  });

  return { generateChallengeMutation };
};

export const useVerifyChallenge = () => {
  const verifyChallengeMutation = useMutation<
    VerifyChallengeResponseDto,
    ApiError,
    VerifyChallengeRequestDto
  >({
    mutationKey: keys.challengeVerification,
    mutationFn: (verifyChallengeRequestDto: VerifyChallengeRequestDto) =>
      verifyChallenge(verifyChallengeRequestDto),
  });

  return { verifyChallengeMutation };
};

export const useRegister = () => {
  const { i18n } = useTranslation();
  const locale = i18n.language == "km" ? "khm" : "eng";

  const registerMutation = useMutation<
    RegistrationResponseDto,
    ApiError,
    RegistrationRequestDto
  >({
    mutationKey: keys.registration,
    mutationFn: (registrationRequestDto: RegistrationRequestDto) =>{
      registrationRequestDto.request.locale = locale
      return register(registrationRequestDto)},
    gcTime: Infinity,
  });

  return { registerMutation };
};

export const useResetPassword = () => {
  const { i18n } = useTranslation();
  const locale = i18n.language == "km" ? "khm" : "eng";

  const resetPasswordMutation = useMutation<
    ResetPasswordResponseDto,
    ApiError,
    ResetPasswordRequestDto
  >({
    mutationKey: keys.resetPassword,
    mutationFn: (resetPasswordRequestDto: ResetPasswordRequestDto) => {
        resetPasswordRequestDto.request.locale = locale
        return resetPassword(resetPasswordRequestDto)},
    gcTime: Infinity,
  });

  return { resetPasswordMutation };
};
