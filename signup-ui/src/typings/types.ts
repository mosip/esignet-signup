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
  response: any;
  errors: Error[] | null;
}

export interface BaseRequestDto {
  requestTime: string;
  request: { [key: string]: any };
}

interface SettingsConfig {
  "username.pattern": string;
  "username.prefix": string;
  "captcha.site.key": string;
  "otp.length": number;
  "password.pattern": string;
  "challenge.timeout": number;
  "resend.attempts": number;
  "resend.delay": number;
  "fullname.pattern": string;
  "status.deferred.response.timeout": number;
  "status.check.limit": number;
}

export interface Settings {
  configs: SettingsConfig;
}

export interface SettingsDto extends BaseResponseDto {
  response: Settings;
}

export type GenerateChallengeRequestDto = BaseRequestDto & {
  request: {
    identifier: string;
    captchaToken: string;
  };
};

export type GenerateChallengeResponseDto = BaseResponseDto & {
  response: {
    status: string;
  };
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
  };
};

export interface LanguageTaggedValue {
  language: string;
  value: string;
}

export interface UserInfo {
  fullName: LanguageTaggedValue[];
  phone: string;
  issueDate: string;
  expireDate: string;
  gender: string;
  dateOfBirth: string;
  address: LanguageTaggedValue[];
}

export type RegisterRequestDto = BaseRequestDto & {
  request: {
    username: string;
    password: string;
    consent: string;
    userInfo: UserInfo;
  };
};

export type RegisterResponseDto = BaseResponseDto & {
  response: {
    status: string;
  };
};

export type RegisterStatusResponseDto = BaseResponseDto & {
  response: {
    status: string;
  };
};
