import { useCallback, useEffect, useRef, useState } from "react";
import ReCAPTCHA from "react-google-recaptcha";
import { useFormContext, UseFormReturn } from "react-hook-form";

import { Button } from "~components/ui/button";
import { FormControl, FormField, FormItem } from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import { Input } from "~components/ui/input";
import { cn } from "~utils/cn";
import { Error, GenerateChallengeRequestDto } from "~typings/types";

import { useGenerateChallenge } from "../mutations";
import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";

interface PhoneProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}
export const Phone = ({ methods }: PhoneProps) => {
  const [hasError, setHasError] = useState<boolean>(false);
  const { control, setValue, getValues } = useFormContext();
  const { setActiveStep } = useSignUpContext();
  const { generateChallengeMutation } = useGenerateChallenge();
  const _reCaptchaRef = useRef<ReCAPTCHA>(null);
  const [error, setError] = useState<Error | null>(null);

  const { trigger } = methods;

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
    <div className="login-container container max-w-lg border-[1px] rounded-2xl bg-white p-0">
      <div className="w-full flex items-center justify-items-start my-4 panel-header">
        <Icons.back className="ml-4 cursor-pointer" />
        <label className="w-full font-medium main-label min-padding-left">
          Please enter your mobile number to continue
        </label>
      </div>
      <hr />
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
            <span className="text-muted-foreground border-r-[1px] border-input px-3">
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
                      placeholder="Enter a 8-digit mobile number"
                      className="outline-none border-none"
                    />
                  </FormControl>
                </FormItem>
              )}
            />
          </div>
          <div
            id="captcha"
            className="flex items-center justify-center"
          >
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
          id="submitButton"
          className="w-full p-4 font-semibold text-white"
          variant="secondary"
          onClick={handleContinue}
        >
          Continue
        </Button>
      </div>
    </div>
  );
};