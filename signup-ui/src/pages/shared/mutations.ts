import { useMutation } from "@tanstack/react-query";

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
  const registerMutation = useMutation<
    RegistrationResponseDto,
    ApiError,
    RegistrationRequestDto
  >({
    mutationKey: keys.registration,
    mutationFn: (registrationRequestDto: RegistrationRequestDto) =>
      register(registrationRequestDto),
    gcTime: Infinity,
  });

  return { registerMutation };
};

export const useResetPassword = () => {
  const resetPasswordMutation = useMutation<
    ResetPasswordResponseDto,
    ApiError,
    ResetPasswordRequestDto
  >({
    mutationKey: keys.resetPassword,
    mutationFn: (resetPasswordRequestDto: ResetPasswordRequestDto) =>
      resetPassword(resetPasswordRequestDto),
    gcTime: Infinity,
  });

  return { resetPasswordMutation };
};
