import {
  KeyboardEvent,
  MouseEvent,
  useCallback,
  useMemo,
  useRef,
  useState,
} from "react";
import ReCAPTCHA from "react-google-recaptcha";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { RESET_PASSWORD } from "~constants/routes";
import { ActionMessage } from "~components/ui/action-message";
import { Button } from "~components/ui/button";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import { Input } from "~components/ui/input";
import {
  Step,
  StepAlert,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { cn } from "~utils/cn";
import { handleInputFilter } from "~utils/input";
import { getLocale } from "~utils/language";
import { getSignInRedirectURL } from "~utils/link";
import { useGenerateChallenge } from "~pages/shared/mutations";
import {
  Error,
  GenerateChallengeRequestDto,
  ResetPasswordForm,
  SettingsDto,
} from "~typings/types";
import { langCodeMappingSelector, useLanguageStore } from "~/useLanguageStore";

import { resetPasswordFormDefaultValues } from "../ResetPasswordPage";
import {
  resendOtpSelector,
  ResetPasswordStep,
  setCriticalErrorSelector,
  setResendOtpSelector,
  setStepSelector,
  useResetPasswordStore,
} from "../useResetPasswordStore";

interface UserInfoProps {
  methods: UseFormReturn<ResetPasswordForm, any, undefined>;
  settings: SettingsDto;
}

export const UserInfo = ({ settings, methods }: UserInfoProps) => {
  const { i18n, t } = useTranslation();

  const _reCaptchaRef = useRef<ReCAPTCHA>(null);
  const { hash: fromSignInHash } = useLocation();
  const { control, setValue, getValues } = useFormContext();
  const [challengeGenerationError, setChallengeGenerationError] =
    useState<Error | null>(null);

  const {
    trigger,
    formState: {
      errors: UserInfoFormErrors,
      isValid: isUserInfoValid,
      isDirty: isUserInfoDirty,
    },
  } = methods;

  const { setStep, setCriticalError, resendOtp, setResendOtp } =
    useResetPasswordStore(
      useCallback(
        (state) => ({
          setStep: setStepSelector(state),
          setCriticalError: setCriticalErrorSelector(state),
          resendOtp: resendOtpSelector(state),
          setResendOtp: setResendOtpSelector(state),
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

  const { generateChallengeMutation } = useGenerateChallenge();

  const handleReCaptchaChange = (token: string | null) => {
    setValue("captchaToken", token ?? "", { shouldValidate: true });
  };

  const handleReCaptchaExpired = () => {
    setValue("captchaToken", "", { shouldValidate: true });
  };

  const handleUsernameInput = (event: KeyboardEvent<HTMLInputElement>) =>
    handleInputFilter(
      event,
      settings.response.configs["identifier.allowed.characters"]
    );

  const handleFullNameInput = (event: KeyboardEvent<HTMLInputElement>) =>
    handleInputFilter(
      event,
      settings.response.configs["fullname.allowed.characters"]
    );

  const disabledContinue =
    !isUserInfoValid ||
    !isUserInfoDirty ||
    getValues("username") === resetPasswordFormDefaultValues.username ||
    getValues("fullname") === resetPasswordFormDefaultValues.fullname ||
    (settings.response.configs["send-challenge.captcha.required"] &&
    getValues("captchaToken") === resetPasswordFormDefaultValues.captchaToken);

  const handleContinue = useCallback(
    async (e: MouseEvent<HTMLButtonElement>) => {
      e.preventDefault();

      if (generateChallengeMutation.isPending) return;

      const isStepValid = await trigger();

      if (isStepValid) {
        const generateChallengeRequestDto: GenerateChallengeRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            identifier: `${
              settings.response.configs["identifier.prefix"]
            }${getValues("username")}`,
            fullname: getValues("fullname"),
            captchaToken: getValues("captchaToken"),
            locale: getLocale(i18n.language, langCodeMapping),
            regenerateChallenge: resendOtp ? true : false,
            purpose: "RESET_PASSWORD",
          },
        };

        return generateChallengeMutation.mutate(generateChallengeRequestDto, {
          onSuccess: ({ response, errors }) => {
            if (errors.length > 0) {
              if (errors[0].errorCode === "invalid_transaction") {
                setCriticalError(errors[0]);
              } else {
                setChallengeGenerationError(errors[0]);
              }
              _reCaptchaRef.current?.reset();
              setValue("captchaToken", "", { shouldValidate: true });
            }

            if (response && errors.length === 0) {
              setStep(ResetPasswordStep.Otp);
            }
          },
        });
      }
    },
    [generateChallengeMutation]
  );

  return (
    <div className="my-10 sm:mb-10 sm:mt-0">
      <Step>
        <StepHeader>
          <StepTitle className="relative flex w-full items-center justify-center gap-x-4 font-semibold">
            {!!fromSignInHash && (
              <a
                href={getSignInRedirectURL(
                  settings?.response.configs["signin.redirect-url"],
                  fromSignInHash,
                  RESET_PASSWORD
                )}
                className="absolute left-0 cursor-pointer"
              >
                <Icons.back id="back-button" name="back-button" />
              </a>
            )}
            {resendOtp ? (
              <div className="grow px-3 text-center text-[16px] font-semibold tracking-normal xs:px-2">
                {t("captcha_required")}
              </div>
            ) : (
              <div className="text-center text-[26px] font-semibold tracking-normal">
                {t("forgot_password")}
              </div>
            )}
          </StepTitle>
          {!resendOtp && (
            <StepDescription>
              {t("forgot_password_description")}
            </StepDescription>
          )}
        </StepHeader>
        <StepDivider />
        <StepAlert className="relative">
          {/* Error message */}
          <ActionMessage hidden={!challengeGenerationError}>
            <p className="truncate text-xs text-destructive">
              {challengeGenerationError &&
                t(`error_response.${challengeGenerationError.errorCode}`)}
            </p>
            <Icons.close
              className="h-4 w-4 cursor-pointer text-destructive"
              onClick={() => {
                setChallengeGenerationError(null);
              }}
            />
          </ActionMessage>
        </StepAlert>
        <StepContent>
          {/* Phone and reCAPTCHA inputs */}
          <div className="flex flex-col gap-y-6 px-6 sm:px-0">
            <div className="flex flex-col gap-y-6">
              {/* Phone number input */}
              <FormField
                name="username"
                control={control}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t("username")}</FormLabel>
                    <div className="relative w-full rounded-md">
                      <FormControl>
                        <div
                          className={cn(
                            "flex h-[52px] rounded-md border-[1px] border-input",
                            UserInfoFormErrors.username && "border-destructive"
                          )}
                        >
                          <span className="flex self-center border-r-[1px] border-input px-3 text-muted-foreground/60">
                            {settings.response.configs["identifier.prefix"]}
                          </span>
                          <div className="w-full">
                            <Input
                              {...field}
                              id="phone_input"
                              type="tel"
                              placeholder={t("enter_your_number_placeholder")}
                              className="h-[inherit] border-none outline-none"
                              minLength={
                                settings.response.configs[
                                  "identifier.length.min"
                                ]
                              }
                              maxLength={
                                settings.response.configs[
                                  "identifier.length.max"
                                ]
                              }
                              onKeyUp={handleUsernameInput}
                              onKeyDown={handleUsernameInput}
                              disabled={resendOtp}
                            />
                          </div>
                        </div>
                      </FormControl>
                      <FormMessage className="w-full" />
                    </div>
                  </FormItem>
                )}
              />
              {/* Full Name */}
              <FormField
                control={control}
                name="fullname"
                render={({ field }) => (
                  <FormItem className="space-y-0">
                    <div className="space-y-2">
                      <div className="flex items-center gap-1">
                        <FormLabel>{t("full_name")}</FormLabel>
                      </div>
                      <FormControl>
                        <Input
                          {...field}
                          id="fullname"
                          placeholder={t("full_name_placeholder")}
                          className={cn(
                            "h-[52px] py-6",
                            UserInfoFormErrors.fullname && "border-destructive"
                          )}
                          minLength={
                            settings.response.configs["fullname.length.min"]
                          }
                          maxLength={
                            settings.response.configs["fullname.length.max"]
                          }
                          onKeyDown={handleFullNameInput}
                          disabled={resendOtp}
                        />
                      </FormControl>
                    </div>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {/* ReCaptcha */}
              {settings.response.configs["send-challenge.captcha.required"] && (
              <div id="captcha" className="flex items-center justify-center">
                <ReCAPTCHA
                  ref={_reCaptchaRef}
                  onChange={handleReCaptchaChange}
                  onExpired={handleReCaptchaExpired}
                  className="recaptcha"
                  sitekey={settings.response.configs["captcha.site.key"] ?? ""}
                />
              </div>
              )}
            </div>
            <Button
              id="continue-button"
              name="continue-button"
              onClick={handleContinue}
              isLoading={generateChallengeMutation.isPending}
              disabled={disabledContinue}
            >
              {t("continue")}
            </Button>
          </div>
        </StepContent>
      </Step>
    </div>
  );
};
