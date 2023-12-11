const GenerateChallengePossibleErrors = [
  "invalid_transaction",
  "invalid_otp_channel",
  "invalid_captcha",
  "send_otp_failed",
  "active_otp_found",
  "unknown_error",
] as const;

export type GenerateChallengeErrors =
  (typeof GenerateChallengePossibleErrors)[number];

const VerifyChallengePossibleErrors = [
  "invalid_transaction",
  "challenge_failed",
  "invalid_challenge_type",
  "invalid_challenge_format",
  "unknown_error",
  "already-registered",
] as const;

export type VerifyChallengeErrors =
  (typeof VerifyChallengePossibleErrors)[number];

const RegisterPossibleErrors = [
  "invalid_transaction",
  "unknown_error",
] as const;

export type RegisterErrors = (typeof RegisterPossibleErrors)[number];

const RegisterStatusPossibleErrors = [
  "invalid_transaction",
  "timed_out",
  "unknown_error",
] as const;

export type RegisterStatusErrors =
  (typeof RegisterStatusPossibleErrors)[number];

export interface Error {
  errorCode:
    | GenerateChallengeErrors
    | VerifyChallengeErrors
    | RegisterErrors
    | RegisterStatusErrors;
  errorMessage: string;
}

export interface BaseResponseDto {
  responseTime: string;
  errors: Error[];
}

export interface BaseRequestDto {
  requestTime: string;
  request: { [key: string]: any };
}

interface SettingsConfig {
  "identifier.pattern": string;
  "identifier.prefix": string;
  "captcha.site.key": string;
  "otp.length": number;
  "password.pattern": string;
  "challenge.timeout": number;
  "resend.attempts": number;
  "resend.delay": number;
  "fullname.pattern": string;
  "status.deferred.response.timeout": number;
  "status.check.limit": number;
  "status.request.limit": number;
  "status.request.delay": number;
  "popup.timeout": number;
}

export interface Settings {
  configs: SettingsConfig;
}

export type SettingsDto = BaseResponseDto & {
  response: Settings;
  errors: Error[] | null;
};

export type GenerateChallengeRequestDto = BaseRequestDto & {
  request: {
    identifier: string;
    captchaToken: string;
    locale: string;
    regenerate: boolean;
  };
};

export type GenerateChallengeResponseDto = BaseResponseDto & {
  response: {
    status: string;
  } | null;
};

export type VerifyChallengeRequestDto = BaseRequestDto & {
  request: {
    identifier: string;
    challengeInfo: {
      challenge: string;
      format: "alpha-numeric";
    };
  };
};

export type VerifyChallengeResponseDto = BaseResponseDto & {
  response: {
    status: string;
  } | null;
};

export interface LanguageTaggedValue {
  language: string;
  value: string;
}

export interface UserInfo {
  fullName: LanguageTaggedValue[];
  phone: string;
  preferredLang: string;
}

export type RegistrationRequestDto = BaseRequestDto & {
  request: {
    username: string;
    password: string;
    consent: string;
    userInfo: UserInfo;
  };
};

export enum RegistrationStatus {
  PENDING = "PENDING",
  COMPLETED = "COMPLETED",
}

export type RegistrationResponseDto = BaseResponseDto & {
  response: {
    status: RegistrationStatus;
  } | null;
};

export enum RegistrationWithFailedStatus {
  PENDING = "PENDING",
  COMPLETED = "COMPLETED",
  FAILED = "FAILED",
}

export type RegistrationStatusResponseDto = BaseResponseDto & {
  response: {
    status: RegistrationWithFailedStatus;
  } | null;
};
