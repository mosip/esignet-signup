import { useMutation } from "@tanstack/react-query";

import { ApiError } from "~typings/core";
import {
  GenerateChallengeRequestDto,
  GenerateChallengeResponseDto,
  RegistrationRequestDto,
  RegistrationResponseDto,
  VerifyChallengeRequestDto,
  VerifyChallengeResponseDto,
} from "~typings/types";

import { generateChallenge, register, verifyChallenge } from "./service";

export const keys = {
  challengeGeneration: ["challengeGeneration"] as const,
  challengeVerification: ["challengeVerification"] as const,
  registration: ["registration"] as const,
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
    mutationFn: (RegistrationRequestDto: RegistrationRequestDto) =>
      register(RegistrationRequestDto),
    gcTime: Infinity,
  });

  return { registerMutation };
};
