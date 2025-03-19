import { useCallback, useEffect, useRef, useState } from "react";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { Trans, useTranslation } from "react-i18next";
import PinInput from "react-pin-input";
import { useTimer } from "react-timer-hook";

import { criticalErrorsToPopup } from "~constants/criticalErrors";
import { ResendAttempt } from "~components/resend-attempt";
import { ActionMessage } from "~components/ui/action-message";
import { Button } from "~components/ui/button";
import { FormControl, FormField, FormItem } from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import {
  Step,
  StepAlert,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { base64FullName } from "~utils/fullName";
import { getLocale } from "~utils/language";
import { maskPhoneNumber } from "~utils/phone";
import { convertTime, getTimeoutTime } from "~utils/timer";
import {
  useGenerateChallenge,
  useVerifyChallenge,
} from "~pages/shared/mutations";
import {
  Error,
  GenerateChallengeRequestDto,
  ResetPasswordForm,
  ResetPasswordPossibleInvalid,
  SettingsDto,
  VerifyChallengeRequestDto,
} from "~typings/types";
import { langCodeMappingSelector, useLanguageStore } from "~/useLanguageStore";

import { resetPasswordFormDefaultValues } from "../ResetPasswordPage";
import {
  resendAttemptsSelector,
  ResetPasswordStep,
  setCriticalErrorSelector,
  setResendAttemptsSelector,
  setResendOtpSelector,
  setStepSelector,
  stepSelector,
  useResetPasswordStore,
} from "../useResetPasswordStore";

interface OtpProps {
  settings: SettingsDto;
  methods: UseFormReturn<ResetPasswordForm, any, undefined>;
}

export const Otp = ({ methods, settings }: OtpProps) => {
  const { i18n, t } = useTranslation();

  const pinInputRef = useRef<PinInput | null>(null);
  const { control, getValues, setValue } = useFormContext();
  const {
    step,
    setStep,
    setCriticalError,
    setResendOtp,
    resendAttempts,
    setResendAttempts,
  } = useResetPasswordStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
        setResendOtp: setResendOtpSelector(state),
        resendAttempts: resendAttemptsSelector(state),
        setResendAttempts: setResendAttemptsSelector(state),
      }),
      []
    )
  );

  const { langCodeMapping } = useLanguageStore(
    useCallback(
      (state) => ({
        langCodeMapping: langCodeMappingSelector(state),
      }),
      []
    )
  );

  const { trigger, reset, formState, resetField } = methods;
  const [captchaRequired, setCaptchaRequired] = useState<boolean>(false);
  const { generateChallengeMutation } = useGenerateChallenge();
  const { verifyChallengeMutation } = useVerifyChallenge();
  const [challengeVerificationError, setChallengeVerificationError] =
    useState<Error | null>(null);

  var pinInputStyle: object;

  var pinInputStyles = {
    margin: "0px 02px",
    border: "2px solid #C1C1C1",
    color: "#000000",
    borderRadius: "8px",
  };

  if (window.screen.availWidth <= 430) {
    let inputBoxSizeMd = {
      width: "48px",
      height: "48px",
    };
    let inputBoxSizeSm = {
      width: "32px",
      height: "32px",
      overflow: "auto",
    };
    if (settings.response.configs["otp.length"] <= 6) {
      pinInputStyle = { ...pinInputStyles, ...inputBoxSizeMd };
    } else {
      pinInputStyle = { ...pinInputStyles, ...inputBoxSizeSm };
    }
  } else {
    let inputBoxSize = {
      width: "55px",
      height: "52px",
    };
    pinInputStyle = { ...pinInputStyles, ...inputBoxSize };
  }

  const {
    totalSeconds: resendOtpTotalSecs,
    restart: restartResendOtpTotalSecs,
  } = useTimer({
    expiryTimestamp: getTimeoutTime(settings.response.configs["resend.delay"]),
  });

  useEffect(() => {
    resetField("otp", { defaultValue: resetPasswordFormDefaultValues.otp });
  }, [resetField]);

  useEffect(() => {
    if (resendAttempts === null) {
      setResendAttempts(settings.response.configs["resend.attempts"]);
    }
    setCaptchaRequired(
      settings.response.configs["send-challenge.captcha.required"]
    );
  }, [settings.response.configs]);

  useEffect(() => {
    if (!challengeVerificationError) return;

    const intervalId = setInterval(() => {
      setChallengeVerificationError(null);
    }, settings.response.configs["popup.timeout"] * 1000);

    return () => clearInterval(intervalId);
  }, [challengeVerificationError]);

  const handlePinInputRef = (n: PinInput | null) => {
    pinInputRef.current = n;
  };

  const handleOtpComplete = (otp: string) => {
    setValue("otp", otp, { shouldValidate: true });
  };

  const handleOtpChange = (otp: string) => {
    setValue("otp", otp, { shouldValidate: true, shouldTouch: true });
    setChallengeVerificationError(null);
  };

  const handleResendOtp = useCallback(
    (e: any) => {
      e.preventDefault();
      if (settings?.response.configs && resendAttempts > 0) {
        setChallengeVerificationError(null);
        if (captchaRequired) {
          redirectBack(true);
        } else {
          const generateChallengeRequestDto: GenerateChallengeRequestDto = {
            requestTime: new Date().toISOString(),
            request: {
              identifier: `${
                settings.response.configs["identifier.prefix"]
              }${getValues("username")}`,
              captchaToken: getValues("captchaToken"),
              locale: getLocale(i18n.language, langCodeMapping),
              regenerateChallenge: true,
              purpose: "RESET_PASSWORD",
            },
          };

          return generateChallengeMutation.mutate(generateChallengeRequestDto, {
            onSuccess: ({ response, errors }) => {
              pinInputRef.current?.clear();
              setValue("otp", "", { shouldValidate: true });

              if (errors && errors.length > 0) {
                if (errors[0].errorCode === "invalid_transaction") {
                  setCriticalError(errors[0]);
                } else {
                  setChallengeVerificationError(errors[0]);
                }
              }

              if (errors.length === 0 && response?.status === "SUCCESS") {
                setResendAttempts(resendAttempts - 1);
                restartResendOtpTotalSecs(
                  getTimeoutTime(settings.response.configs["resend.delay"])
                );
              }
            },
          });
        }
      }
    },
    [
      settings?.response.configs,
      resendAttempts,
      generateChallengeMutation,
      getValues,
      setValue,
      restartResendOtpTotalSecs,
    ]
  );
  const redirectBack = (reset: boolean) => {
    setResendAttempts(reset ? resendAttempts - 1 : null);
    setResendOtp(reset);

    if (step === ResetPasswordStep.Otp) {
      setValue("captchaToken", "", { shouldValidate: true });
      setValue("otp", "", { shouldValidate: true });
    }

    setStep(ResetPasswordStep.UserInfo);
  };

  const handleBack = useCallback(() => {
    redirectBack(false);
  }, [step, setStep, setValue]);

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();

      if (verifyChallengeMutation.isPending) return;

      const isStepValid = await trigger();

      if (isStepValid) {
        setChallengeVerificationError(null);

        const verifyChallengeRequestDto: VerifyChallengeRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            identifier: `${
              settings.response.configs["identifier.prefix"]
            }${getValues("username")}`,
            challengeInfo: [
              {
                challenge: getValues("otp"),
                format: "alpha-numeric",
                type: "OTP",
              },
              {
                challenge: base64FullName(getValues("fullname"), "khm"),
                format: "base64url-encoded-json",
                type: "KBI",
              },
            ],
          },
        };

        return verifyChallengeMutation.mutate(verifyChallengeRequestDto, {
          onSuccess: ({ errors }) => {
            if (errors.length > 0) {
              if (
                [
                  ...new Set([
                    "invalid_transaction",
                    ...ResetPasswordPossibleInvalid,
                    ...criticalErrorsToPopup
                  ])
                ].includes(errors[0].errorCode)
              ) {
                setCriticalError(errors[0]);
              } else {
                setChallengeVerificationError(errors[0]);
              }
            }

            if (errors.length === 0) {
              setStep(ResetPasswordStep.ResetPassword);
            }
          },
          onError: () => {
            setChallengeVerificationError(null);
          },
        });
      }
    },
    [setStep, trigger, getValues, verifyChallengeMutation]
  );

  const handleExhaustedAttempt = () => {
    setStep(ResetPasswordStep.UserInfo);
    setResendAttempts(settings.response.configs["resend.attempts"]);
    setResendOtp(false);
    reset();
  };

  return (
    <Step>
      <StepHeader className="px-0 sm:px-[18px] sm:pb-[25px] sm:pt-[33px]">
        <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-base font-semibold">
          <Icons.back
            id="back-button"
            name="back-button"
            className="absolute left-0 ml-8 cursor-pointer"
            onClick={handleBack}
          />
          <div className="w-full text-center text-[22px] font-semibold">
            {t("otp_header")}
          </div>
        </StepTitle>
        <StepDescription className="w-full pt-2 tracking-normal">
          <div className="text-muted-neutral-gray">
            {t("otp_subheader", {
              no_of_digit: settings?.response.configs["otp.length"],
            })}
          </div>
          <div className="font-medium text-muted-dark-gray">
            <span>{settings.response.configs["identifier.prefix"]}</span>{" "}
            <span>{maskPhoneNumber(getValues("username"), 4)}</span>
          </div>
        </StepDescription>
      </StepHeader>
      <StepDivider />
      <StepAlert>
        {/* Error message */}
        <ActionMessage hidden={!challengeVerificationError}>
          <p className="truncate text-xs text-destructive">
            {challengeVerificationError &&
              t(`error_response.${challengeVerificationError.errorCode}`)}
          </p>
          <Icons.close
            className="h-4 w-4 cursor-pointer text-destructive"
            onClick={() => setChallengeVerificationError(null)}
          />
        </ActionMessage>
      </StepAlert>
      <StepContent>
        {/* OTP inputs */}
        <div className="flex flex-col gap-y-6 p-6 sm:p-0">
          <FormField
            name="otp"
            control={control}
            render={({ field }) => (
              <FormItem>
                <FormControl>
                  <PinInput
                    ref={handlePinInputRef}
                    length={settings.response.configs["otp.length"]}
                    secret
                    secretDelay={1}
                    focus
                    initialValue={field.value}
                    type="numeric"
                    inputMode="number"
                    style={{
                      display: "flex",
                      justifyContent: "center",
                      padding: "5px 0px",
                      fontSize: "24px",
                    }}
                    inputStyle={pinInputStyle}
                    inputFocusStyle={{
                      border: "2px solid #676766",
                    }}
                    autoSelect={true}
                    onComplete={(value, _) => {
                      handleOtpComplete(value);
                    }}
                    onChange={(value, _) => {
                      handleOtpChange(value);
                    }}
                  />
                </FormControl>
              </FormItem>
            )}
          />
          <Button
            id="verify-otp-button"
            name="verify-otp-button"
            className="w-full p-4 font-semibold"
            onClick={handleContinue}
            disabled={!formState.isValid}
            isLoading={verifyChallengeMutation.isPending}
          >
            {t("verify")}
          </Button>
          <div className="flex flex-col items-center sm:mx-0">
            <div className="flex gap-x-1 text-center">
              <Trans
                i18nKey="resend_otp_detail"
                components={{
                  CountDownSpan: <span className="font-semibold" />,
                }}
                values={{ countDown: convertTime(resendOtpTotalSecs) }}
              />
            </div>
            <Button
              id="resend-otp-button"
              name="resend-otp-button"
              variant="link"
              className="m-1 h-5 text-base font-bold"
              disabled={resendOtpTotalSecs > 0 || resendAttempts === 0}
              onClick={handleResendOtp}
            >
              {t("resend_otp")}
            </Button>
            {resendAttempts !==
              settings.response.configs["resend.attempts"] && (
              <ResendAttempt
                currentAttempts={resendAttempts}
                totalAttempts={settings.response.configs["resend.attempts"]}
                attemptRetryAfter={settings.response.configs["otp.blocked"]}
                showRetry={resendAttempts === 0 && resendOtpTotalSecs === 0}
              />
            )}
            {resendAttempts === 0 && resendOtpTotalSecs === 0 && (
              <Button
                id="landing-page-button"
                name="landing-page-button"
                variant="link"
                className="m-4 h-4 px-12 text-sm"
                onClick={handleExhaustedAttempt}
              >
                {t("go_back_to_landing_page")}
              </Button>
            )}
          </div>
        </div>
      </StepContent>
    </Step>
  );
};
