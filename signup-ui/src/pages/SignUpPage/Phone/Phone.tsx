import { useCallback, useEffect, useRef, useState } from "react";
import ReCAPTCHA from "react-google-recaptcha";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { Button } from "~components/ui/button";
import { FormControl, FormField, FormItem } from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import { Input } from "~components/ui/input";
import {
  Step,
  StepContent,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { cn } from "~utils/cn";
import { getSignInRedirectURL } from "~utils/link";
import { Error, GenerateChallengeRequestDto } from "~typings/types";

import { useGenerateChallenge } from "../mutations";
import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";

interface PhoneProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}
export const Phone = ({ methods }: PhoneProps) => {
  const { t } = useTranslation();

  const [hasError, setHasError] = useState<boolean>(false);
  const { control, setValue, getValues } = useFormContext();
  const { setActiveStep } = useSignUpContext();
  const { generateChallengeMutation } = useGenerateChallenge();
  const _reCaptchaRef = useRef<ReCAPTCHA>(null);
  const [error, setError] = useState<Error | null>(null);
  const { hash: fromSingInHash } = useLocation();

  const { trigger, formState } = methods;

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
              setActiveStep((prevActiveStep) => prevActiveStep + 1);
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
    [generateChallengeMutation, getValues, setActiveStep, trigger, setValue]
  );

  return (
    <Step>
      <StepHeader className="px-0">
        <StepTitle className="relative flex gap-x-4 w-full text-base font-semibold items-center justify-center">
          {!!fromSingInHash && <a href={getSignInRedirectURL(fromSingInHash)} className="absolute left-0 ml-4 cursor-pointer"><Icons.back /></a>}
          <div className="text-center font-semibold tracking-normal">
            {t("enter_your_number")}
          </div>
        </StepTitle>
      </StepHeader>
      <StepDivider />
      <StepContent>
        {/* Error message */}
        <div
          className={cn(
            "flex items-center justify-between bg-destructive/5 px-4 py-2",
            {
              hidden: !hasError,
            }
          )}
        >
          <p className="text-xs text-destructive">{error?.errorMessage}</p>
          <Icons.close
            className="text-destructive h-4 w-4 cursor-pointer"
            onClick={() => setHasError(false)}
          />
        </div>
        {/* Phone and reCAPTCHA inputs */}
        <div className="p-6 flex flex-col gap-y-6">
          <div className="flex flex-col gap-y-3">
            {/* Phone number input */}
            <div
              id="phone"
              className="flex items-center justify-center border-[1px] border-input ring-offset-background rounded-md"
            >
              <span className="text-muted-foreground/60 border-r-[1px] border-input px-3">
                +855
              </span>
              <FormField
                name="phone"
                control={control}
                render={({ field }) => (
                  <FormItem className="w-full">
                    <FormControl>
                      <Input
                        {...field}
                        id="phone_input"
                        type="tel"
                        placeholder={t("enter_your_number_placeholder")}
                        className="outline-none border-none"
                      />
                    </FormControl>
                  </FormItem>
                )}
              />
            </div>
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
            disabled={!formState.isValid}
            isLoading={generateChallengeMutation.isLoading}
          >
            {t("continue")}
          </Button>
        </div>
      </StepContent>
    </Step>
  );
};
