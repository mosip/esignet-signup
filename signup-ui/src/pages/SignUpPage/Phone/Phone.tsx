import { useCallback, useEffect, useRef, useState } from "react";
import ReCAPTCHA from "react-google-recaptcha";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { Button } from "~components/ui/button";
import {
  FormControl,
  FormField,
  FormItem,
  FormMessage,
  useFormField,
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
import { getSignInRedirectURL } from "~utils/link";
import {
  Error,
  GenerateChallengeRequestDto,
  SettingsDto,
} from "~typings/types";

import { useGenerateChallenge } from "../mutations";
import { SignUpForm } from "../SignUpPage";
import { setStepSelector, SignUpStep, useSignUpStore } from "../useSignUpStore";

interface PhoneProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}
export const Phone = ({ settings, methods }: PhoneProps) => {
  const { t } = useTranslation();

  const { setStep } = useSignUpStore(
    useCallback((state) => ({ setStep: setStepSelector(state) }), [])
  );
  const [hasError, setHasError] = useState<boolean>(false);
  const { control, setValue, getValues } = useFormContext();
  const { generateChallengeMutation } = useGenerateChallenge();
  const _reCaptchaRef = useRef<ReCAPTCHA>(null);
  const [error, setError] = useState<Error | null>(null);
  const { hash: fromSignInHash } = useLocation();

  const {
    trigger,
    formState: { errors: formError, isValid },
  } = methods;

  useEffect(() => {
    if (!hasError) return;

    const intervalId = setInterval(() => {
      setHasError(false);
    }, 3 * 1000);

    return () => clearInterval(intervalId);
  }, [hasError]);

  const handleReCaptchaChange = (token: string | null) => {
    setValue("captchaToken", token ?? "", { shouldValidate: true });
  };

  const handleReCaptchaExpired = () => {
    setValue("captchaToken", "", { shouldValidate: true });
  };

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();
      const isStepValid = await trigger();

      if (isStepValid) {
        const generateChallengeRequestDto: GenerateChallengeRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            identifier: getValues("phone"),
            captchaToken: getValues("captchaToken"),
          },
        };

        return generateChallengeMutation.mutate(generateChallengeRequestDto, {
          onSuccess: ({ errors }) => {
            if (!errors) {
              setValue("otp", "", { shouldValidate: true });
              setStep(SignUpStep.Otp);
            }

            if (errors) {
              setError(errors[0]);
              setHasError(true);
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
      <StepHeader className="px-0">
        <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-base font-semibold">
          {!!fromSignInHash && (
            <a
              href={getSignInRedirectURL(fromSignInHash)}
              className="absolute left-0 ml-6 cursor-pointer"
            >
              <Icons.back />
            </a>
          )}
          <div className="text-center font-semibold tracking-normal">
            {t("enter_your_number")}
          </div>
        </StepTitle>
      </StepHeader>
      <StepDivider />
      <StepAlert className="relative">
        {/* Error message */}
        <div
          className={cn(
            "absolute flex w-full items-center justify-between bg-destructive/5 px-4 py-2",
            {
              hidden: !hasError,
            }
          )}
        >
          <p className="text-xs text-destructive">{error?.errorMessage}</p>
          <Icons.close
            className="h-4 w-4 cursor-pointer text-destructive"
            onClick={() => setHasError(false)}
          />
        </div>
      </StepAlert>
      <StepContent>
        {/* Phone and reCAPTCHA inputs */}
        <div className="flex flex-col gap-y-6 p-6">
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
                          "flex h-[52px] rounded-md border-[1px] border-input",
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
                sitekey={process.env.REACT_APP_CAPTCHA_SITE_KEY ?? ""}
              />
            </div>
          </div>
          <Button
            onClick={handleContinue}
            disabled={!isValid}
            isLoading={generateChallengeMutation.isPending}
          >
            {t("continue")}
          </Button>
        </div>
      </StepContent>
    </Step>
  );
};
