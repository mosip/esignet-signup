import {
  KeyboardEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import ReCAPTCHA from "react-google-recaptcha";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ActionMessage } from "~components/ui/action-message";
import { Button } from "~components/ui/button";
import {
  FormControl,
  FormField,
  FormItem,
  FormMessage,
} from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import { Input } from "~components/ui/input";
import {
  Step,
  StepAlert,
  StepContent,
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
  SettingsDto,
} from "~typings/types";
import { langCodeMappingSelector, useLanguageStore } from "~/useLanguageStore";

import { SignUpForm, signUpFormDefaultValues } from "../SignUpPage";
import {
  resendOtpSelector,
  setCriticalErrorSelector,
  setResendOtpSelector,
  setStepSelector,
  SignUpStep,
  useSignUpStore,
} from "../useSignUpStore";

interface PhoneProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}
export const Phone = ({ settings, methods }: PhoneProps) => {
  const { i18n, t } = useTranslation();
  const { setStep, setCriticalError, resendOtp, setResendOtp } = useSignUpStore(
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

  const [hasError, setHasError] = useState<boolean>(false);
  const { control, setValue, getValues } = useFormContext();
  const { generateChallengeMutation } = useGenerateChallenge();
  const _reCaptchaRef = useRef<ReCAPTCHA>(null);
  const [error, setError] = useState<Error | null>(null);
  const { hash: fromSignInHash } = useLocation();

  const {
    trigger,
    formState: { errors: formError, isValid, isDirty },
  } = methods;

  useEffect(() => {
    if (!hasError) return;

    const intervalId = setInterval(() => {
      setHasError(false);
    }, settings.response.configs["popup.timeout"] * 1000);

    return () => clearInterval(intervalId);
  }, [hasError]);

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

  const disabledContinue =
    !isValid ||
    !isDirty ||
    getValues("phone") === signUpFormDefaultValues.phone ||
    getValues("captchaToken") === signUpFormDefaultValues.captchaToken;

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();

      if (generateChallengeMutation.isPending) return;

      const isStepValid = await trigger();

      if (isStepValid) {
        setHasError(false);

        const generateChallengeRequestDto: GenerateChallengeRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            identifier: `${
              settings.response.configs["identifier.prefix"]
            }${getValues("phone")}`,
            captchaToken: getValues("captchaToken"),
            locale: getLocale(i18n.language, langCodeMapping),
            regenerateChallenge: false,
            purpose: "REGISTRATION",
          },
        };

        return generateChallengeMutation.mutate(generateChallengeRequestDto, {
          onSuccess: ({ response, errors }) => {
            if (errors.length > 0) {
              if (errors[0].errorCode === "invalid_transaction") {
                setCriticalError(errors[0]);
              } else {
                setError(errors[0]);
                setHasError(true);
              }
            }

            if (response && errors.length === 0) {
              setStep(SignUpStep.Otp);
              setResendOtp(false);
            }
          },
          onError: () => {
            setHasError(true);
          },
        });
      }
    },
    [generateChallengeMutation, getValues, setStep, trigger, setValue]
  );

  return (
    <Step>
      <StepHeader className="px-6">
        <StepTitle className="flex w-full items-center justify-center text-base font-semibold leading-5">
          {!!fromSignInHash && (
            <a
              href={getSignInRedirectURL(
                settings?.response.configs["signin.redirect-url"],
                fromSignInHash,
                "/signup"
              )}
              className="flex-none cursor-pointer"
            >
              <Icons.back id="back-button" name="back-button" />
            </a>
          )}
          {resendOtp ? 
          <div className="grow px-3 xs:px-2 text-center font-semibold tracking-normal">
            {t("captcha_required")}
          </div> :
          <div className="grow px-3 xs:px-2 text-center font-semibold tracking-normal">
            {t("enter_your_number")}
          </div>}
        </StepTitle>
      </StepHeader>
      <StepDivider />
      <StepAlert className="relative">
        {/* Error message */}
        <ActionMessage hidden={!hasError}>
          <p className="truncate text-xs text-destructive">
            {error && t(`error_response.${error.errorCode}`)}
          </p>
          <Icons.close
            className="h-4 w-4 cursor-pointer text-destructive"
            onClick={() => {
              setHasError(false);
            }}
          />
        </ActionMessage>
      </StepAlert>
      <StepContent>
        {/* Phone and reCAPTCHA inputs */}
        <div className="flex flex-col gap-y-6 p-6 sm:p-0">
          <div className="flex flex-col gap-y-3">
            {/* Phone number input */}
            <FormField
              name="phone"
              control={control}
              render={({ field }) => (
                <FormItem>
                  <div className="relative w-full rounded-md">
                    <FormControl>
                      <div
                        className={cn(
                          "input flex rounded-md border-[1px] border-input",
                          formError.phone && "border-destructive"
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
                              settings.response.configs["identifier.length.min"]
                            }
                            maxLength={
                              settings.response.configs["identifier.length.max"]
                            }
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
            <div id="captcha" className="flex items-center justify-center">
              {/* I'm not a robot checkbox */}
              <ReCAPTCHA
                ref={_reCaptchaRef}
                onChange={handleReCaptchaChange}
                onExpired={handleReCaptchaExpired}
                className="recaptcha"
                sitekey={settings.response.configs["captcha.site.key"] ?? ""}
              />
            </div>
          </div>
          <Button
            id="continue-button"
            name="continue-button"
            onClick={handleContinue}
            disabled={disabledContinue}
            isLoading={generateChallengeMutation.isPending}
          >
            {t("continue")}
          </Button>
        </div>
      </StepContent>
    </Step>
  );
};
