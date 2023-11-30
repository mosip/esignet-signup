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

export const useGenerateChallenge = () => {
  const generateChallengeMutation = useMutation<
    GenerateChallengeResponseDto,
    ApiError,
    GenerateChallengeRequestDto
  >({
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
    mutationFn: (RegistrationRequestDto: RegistrationRequestDto) =>
      register(RegistrationRequestDto),
  });

  return { registerMutation };
};
