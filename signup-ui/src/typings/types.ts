export interface ResetPasswordForm {
  username: string;
  fullname: string;
  captchaToken: string;
  otp: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface EkYCVerificationForm {
  consent: EKYCConsentStatus;
  disabilityType: DisabilityType;
  verifierId: string;
}

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
  "identifier_already_registered",
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

export const ResetPasswordPossibleInvalid = [
  "knowledgebase_mismatch",
  "identifier_not_found",
  "invalid_kbi_challenge",
  "challenge_format_and_type_mismatch",
  "kbi_challenge_not_found"
];

const ResetPasswordPossibleErrors = [
  "invalid_transaction",
  "not_registered",
  "invalid_identifier",
  "invalid_password",
  "invalid_request",
  "reset_pwd_failed",
  "knowledgebase_mismatch",
  "identifier_not_found",
  "invalid_kbi_challenge",
  "challenge_format_and_type_mismatch",
  "kbi_challenge_not_found",
] as const;

export type ResetPasswordErrors = (typeof ResetPasswordPossibleErrors)[number];

const SlotAvailabilityPossibleErrors = [
  "invalid_transaction",
  "invalid_identifier",
  "invalid_password",
  "invalid_request",
  "reset_pwd_failed",
  "slot_unavailable",
  "slot_not_available",
] as const;

export type SlotAvailabilityErrors =
  (typeof SlotAvailabilityPossibleErrors)[number];

const IdentityVerificationStatusPossibleErrors = [
  "invalid_transaction",
  "unknown_error",
] as const;

export type IdentityVerificationStatusErrors =
  (typeof IdentityVerificationStatusPossibleErrors)[number];
export interface Error {
  errorCode:
    | GenerateChallengeErrors
    | VerifyChallengeErrors
    | RegisterErrors
    | RegisterStatusErrors
    | ResetPasswordErrors
    | SlotAvailabilityErrors
    | IdentityVerificationStatusErrors;
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

export interface SettingsConfig {
  "identifier.pattern": string;
  "identifier.prefix": string;
  "captcha.site.key": string;
  "otp.length": number;
  "otp.secret": boolean;
  "otp.blocked": number;
  "password.pattern": string;
  "password.length.max": number;
  "password.length.min": number;
  "challenge.timeout": number;
  "resend.attempts": number;
  "resend.delay": number;
  "fullname.pattern": string;
  "status.request.limit": number;
  "status.request.delay": number;
  "status.request.retry.error.codes": string;
  "slot.request.limit": number;
  "slot.request.delay": number;
  "popup.timeout": number;
  "signin.redirect-url": string;
  "identifier.allowed.characters": string;
  "identifier.length.min": number;
  "identifier.length.max": number;
  "fullname.allowed.characters": string;
  "fullname.length.min": number;
  "fullname.length.max": number;
  "send-challenge.captcha.required": boolean;
  "signup.oauth-client-id": string;
  "identity-verification.redirect-url": string;
  "online.polling.timeout": number;
  "offline.polling.interval": number;
  "offline.polling.timeout": number;
  "offline.polling.enabled": boolean;
  "offline.polling.url": string;
  "broswer.minimum-version": { [key: string]: string; };
  "esignet-consent.redirect-url": string;
}

export interface Settings {
  configs: SettingsConfig;
}

export type SettingsDto = BaseResponseDto & {
  response: Settings;
  errors: Error[] | null;
};

const ChallengeGenerationPurposes = ["REGISTRATION", "RESET_PASSWORD"] as const;

export type ChallengeGenerationPurpose =
  (typeof ChallengeGenerationPurposes)[number];

type BaseChallengeGenerationRequest = {
  identifier: string;
  captchaToken: string;
  locale: string;
  regenerateChallenge: boolean;
};

type RegistrationChallengeGenerationRequest = BaseChallengeGenerationRequest & {
  purpose: "REGISTRATION";
};

type ResetPasswordChallengeGenerationRequest =
  BaseChallengeGenerationRequest & {
    purpose: "RESET_PASSWORD";
  };

export type GenerateChallengeRequestDto = BaseRequestDto & {
  request:
    | RegistrationChallengeGenerationRequest
    | ResetPasswordChallengeGenerationRequest;
};

export type GenerateChallengeResponseDto = BaseResponseDto & {
  response: {
    status: string;
  } | null;
};

const ChallengeInfoFormats = [
  "alpha-numeric",
  "base64url-encoded-json",
] as const;

type ChallengeInfoFormatType = (typeof ChallengeInfoFormats)[number];

export interface ChallengeInfoDto {
  challenge: string;
  format: ChallengeInfoFormatType;
  type: string;
}

export type VerifyChallengeRequestDto = BaseRequestDto & {
  request: {
    identifier: string;
    challengeInfo: ChallengeInfoDto[];
  };
};

export type VerifyChallengeResponseDto = BaseResponseDto & {
  response: {
    status: string;
  } | null;
};

export type KeyValueStringObject = {
  [key: string]: string | KeyValueStringObject;
};

export type KycProviderDetailProp = "terms&Conditions" | "previewInfo" | "stepCodes" | "errors" | "messages";

export type KycProviderDetail = {
  "terms&Conditions": KeyValueStringObject;
  previewInfo?: KeyValueStringObject;
  stepCodes?: KeyValueStringObject;
  errors: { [key: string]: KeyValueStringObject } | null;
  messages: { [key: string]: KeyValueStringObject } | null;
};

export type KycProviderDetailResponseDto = BaseResponseDto & {
  response: KycProviderDetail;
};

export type SlotAvailabilityDto = BaseResponseDto & {
  response: {
    status: string;
    message: string;
  } | null;
};

export type KycProvidersResponseDto = BaseResponseDto & {
  response: {
    identityVerifiers: KycProvider[];
  };
};

export interface KycProvider {
  id: string;
  displayName: KeyValueStringObject;
  logoUrl: string;
  description: string;
  processType: string;
  active: boolean;
  retryOnFailure: boolean;
  resumeOnSuccess: boolean;
}

export interface IdvStep {
  code: string;
  framesPerSecond: number;
  durationInSeconds: number;
  startupDelayInSeconds: number;
  retryOnTimeout: boolean;
  retryableErrorCodes: string[];
}

export enum IdvFeedbackEnum {
  MESSAGE = "MESSAGE",
  ERROR = "ERROR",
  COLOR = "COLOR",
}

export type IdvFeedbackType = keyof typeof IdvFeedbackEnum;

export interface IdvFeedback {
  type: IdvFeedbackType | string;
  code: string;
}

export interface IdvFrames {
  frame: string;
  order: number;
}
export interface IdentityVerificationResponseDto {
  slotId: string;
  step?: IdvStep | null;
  feedback?: IdvFeedback | null;
}

export interface IdentityVerificationRequestDto {
  slotId: string;
  stepCode?: string | null;
  frames?: IdvFrames[];
}

export interface IdentityVerificationState {
  stepCode: string | null;
  fps: number | null;
  totalDuration: number | null;
  startupDelay: number;
  feedbackType: string | null;
  feedbackCode: string | null;
}

export interface DefaultEkyVerificationProp {
  settings: Settings;
  cancelPopup: (cancelProp: CancelPopup) => any;
}

export interface CancelPopup {
  cancelButton: boolean;
  handleStay: () => void;
}

export interface SignupHashCode {
  state: string;
  code: string;
}

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
    locale: string | null;
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

export type ResetPasswordRequestDto = BaseRequestDto & {
  request: {
    identifier: string;
    password: string;
    locale: string | null;
  };
};

export enum ResetPasswordStatus {
  PENDING = "PENDING",
  COMPLETED = "COMPLETED",
}

export type ResetPasswordResponseDto = BaseResponseDto & {
  response: {
    status: ResetPasswordStatus;
  } | null;
};

const EKYCConsentOptions = ["AGREE"] as const;

type EKYCConsentStatus = (typeof EKYCConsentOptions)[number];

const DisabilityOptions = [
  "VISION",
  "AUDITORY",
  "MOBILITY",
  "NEUROLOGICAL",
] as const;

type DisabilityType = (typeof DisabilityOptions)[number] | null;

export type SlotAvailabilityRequestDto = BaseRequestDto & {
  request: {
    verifierId: string;
    consent: EKYCConsentStatus;
    disabilityType?: DisabilityType;
  };
};

export type SlotAvailabilityResponseDto = BaseResponseDto & {
  response: {
    slotId: string;
  };
};

export type UpdateProcessRequestDto = BaseRequestDto & {
  request: {
    authorizationCode: string;
    state: string;
  };
};

export type UpdateProcessResponseDto = BaseResponseDto & {
  response: {
    status: object;
  } | null;
};

export enum IdentityVerificationStatus {
  UPDATEPENDING = "UPDATE_PENDING",
  COMPLETED = "COMPLETED",
  FAILED = "FAILED",
}

export type IdentityVerificationStatusResponseDto = BaseResponseDto & {
  response: {
    status: IdentityVerificationStatus;
  } | null;
  errors: IdentityVerificationStatusErrors;
};
