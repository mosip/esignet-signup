import { useCallback, useEffect, useRef, useState } from "react";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import PinInput from "react-pin-input";
import { useLocation } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "~components/ui/alert-dialog";
import { Button } from "~components/ui/button";
import { FormControl, FormField, FormItem } from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import {
  Step,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { cn } from "~utils/cn";
import { getSignInRedirectURL } from "~utils/link";
import { maskPhoneNumber } from "~utils/phone";
import { convertTime } from "~utils/timer";
import {
  Error,
  GenerateChallengeRequestDto,
  VerifyChallengeRequestDto,
} from "~typings/types";

import { useGenerateChallenge, useVerifyChallenge } from "../mutations";
import { useSettings } from "../queries";
import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";
import { ResendAttempt } from "./components/ResendAttempt";
import { useTimer } from "./hooks/useTimer";

interface OTPProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const OTP = ({ methods }: OTPProps) => {
  const { t } = useTranslation();

  const { data: settings, isLoading } = useSettings();
  const [hasError, setHasError] = useState<boolean>(false);
  const pinInputRef = useRef<PinInput | null>(null);
  const { control, getValues, setValue } = useFormContext();
  const { setActiveStep } = useSignUpContext();
  const { trigger, reset, formState } = methods;
  const [resendAttempts, setResendAttempts] = useState<number>(0);
  const [enableResendOtp, setEnableResendOtp] = useState<boolean>(false);
  const [timeLeft, setTimeLeft] = useTimer(0, setEnableResendOtp);
  const { generateChallengeMutation } = useGenerateChallenge();
  const { verifyChallengeMutation } = useVerifyChallenge();
  const [error, setError] = useState<Error | null>(null);
  const [showDialog, setShowDialog] = useState(false);
  const { hash: fromSingInHash } = useLocation();

  useEffect(() => {
    if (settings?.response.configs) {
      setTimeLeft(settings.response.configs["resend.delay"]);
      setResendAttempts(settings.response.configs["resend.attempts"]);
    }
  }, [settings?.response.configs, setTimeLeft]);

  useEffect(() => {
    if (!hasError) return;

    const intervalId = setInterval(() => {
      setHasError(false);
    }, 3 * 1000);

    return () => clearInterval(intervalId);
  }, [hasError]);

  const handlePinInputRef = (n: PinInput | null) => {
    pinInputRef.current = n;
  };

  const handleOtpComplete = (otp: string) => {
    setValue("otp", otp, { shouldValidate: true });
  };

  const handleOtpChange = (otp: string) => {
    setValue("otp", otp, { shouldValidate: true, shouldTouch: true });
  };

  const handleResendOtp = useCallback(
    (e: any) => {
      e.preventDefault();
      if (settings?.response.configs && resendAttempts > 0) {
        const generateChallengeRequestDto: GenerateChallengeRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            identifier: getValues("phone"),
            captchaToken: getValues("captchaToken"),
          },
        };

        return generateChallengeMutation.mutate(generateChallengeRequestDto, {
          onSuccess: ({ errors }) => {
            setResendAttempts((resendAttempt) => resendAttempt - 1);
            setTimeLeft(settings.response.configs["resend.delay"]);
            setEnableResendOtp(false);

            if (errors) {
              setError(errors[0]);
            }
          },
        });
      }
    },
    [
      settings?.response.configs,
      resendAttempts,
      generateChallengeMutation,
      getValues,
      setTimeLeft,
    ]
  );

  const handleBack = useCallback(() => {
    setActiveStep((prevActiveStep) => {
      if (prevActiveStep === 1) {
        setValue("captchaToken", "", { shouldValidate: true });
      }
      return prevActiveStep - 1;
    });
  }, [setActiveStep, setValue]);

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();
      const isStepValid = await trigger();

      if (isStepValid) {
        const verifyChallengeRequestDto: VerifyChallengeRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            identifier: getValues("phone"),
            challengeInfo: {
              challenge: getValues("otp"),
              format: "alpha-numeric",
            },
          },
        };

        return verifyChallengeMutation.mutate(verifyChallengeRequestDto, {
          onSuccess: ({ errors }) => {
            if (!errors) {
              setActiveStep((prevActiveStep) => prevActiveStep + 1);
            }

            if (errors) {
              setError(errors[0]);
            }
          },
          onError: () => {
            setHasError(true);
          },
        });
      }
    },
    [setActiveStep, trigger, getValues, verifyChallengeMutation]
  );

  const handleErrorRedirect = () => {
    if (error?.errorCode === "already-registered" && !!fromSingInHash) {
      window.location.href = getSignInRedirectURL(fromSingInHash);
    } else {
      setActiveStep(0);
      reset();
    }
  };

  const handleExhaustedAttempt = () => {
    setActiveStep(0);
    reset();
  };

  return (
    <>
      <AlertDialog open={!!error}>
        <AlertDialogContent>
          <AlertDialogHeader className="m-2">
            <AlertDialogTitle className="flex flex-col items-center justify-center gap-y-4">
              <FailedIconSvg />
              Error!
            </AlertDialogTitle>
            <AlertDialogDescription className="text-center text-muted-dark-gray">
              {error?.errorMessage}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogAction
              onClick={handleErrorRedirect}
              className="w-full bg-orange-500"
            >
              {error?.errorCode === "already-registered" &&
              !!fromSingInHash
                ? t("login")
                : t("okay")}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
      <Step>
        <StepHeader className="px-0">
          <StepTitle className="relative flex gap-x-4 w-full text-base font-semibold items-center justify-center">
            <Icons.back
              className="absolute left-0 ml-4 cursor-pointer"
              onClick={handleBack}
            />
            <h3 className="w-full font-bold text-[26px] text-center">
              {t("otp_header")}
            </h3>
          </StepTitle>
          <StepDescription className="w-full py-4 tracking-normal">
            <div className="text-muted-neutral-gray">
              {t("otp_subheader", {
                no_of_digit: settings?.response.configs["otp.length"],
              })}
            </div>
            <div className="text-muted-dark-gray">
              <span>+855</span>{" "}
              <span>{maskPhoneNumber(getValues("phone"), 4)}</span>
            </div>
          </StepDescription>
        </StepHeader>
        <StepDivider />
        <StepContent>
          {/* Error message */}
          <div
            className={cn(
              "flex items-center justify-between bg-destructive/5 px-4 py-2",
              {
                hidden: !error,
              }
            )}
          >
            <p className="text-xs text-destructive">{error?.errorMessage}</p>
            <Icons.close
              className="text-destructive h-4 w-4 cursor-pointer"
              onClick={() => setHasError(false)}
            />
          </div>
          {/* OTP inputs */}
          <div className="p-6 flex flex-col gap-y-6">
            {settings && (
              <>
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
                          focus
                          initialValue={field.value}
                          type="numeric"
                          inputMode="number"
                          style={{
                            display: "flex",
                            justifyContent: "space-between",
                            padding: "5px 0px",
                          }}
                          inputStyle={{
                            width: "40px",
                            height: "40px",
                            margin: "0px 0px",
                            border: "2px solid #C1C1C1",
                            color: "#000000",
                            borderRadius: "6px",
                          }}
                          inputFocusStyle={{ border: "2px solid #676766" }}
                          autoSelect={true}
                          onComplete={(value, _) => {
                            //TO handle case when user pastes OTP
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
                  className="w-full p-4 font-semibold"
                  onClick={handleContinue}
                  disabled={!formState.isValid}
                  isLoading={verifyChallengeMutation.isLoading}
                >
                  {t("continue")}
                </Button>
                <div className="flex flex-col items-center mx-12">
                  <div className="flex gap-x-1">
                    {t("resend_otp_detail")}
                    <span className="font-semibold">
                      {convertTime(timeLeft)}
                    </span>
                  </div>
                  <Button
                    variant="link"
                    className="font-bold text-secondary disabled:bg-white disabled:text-muted"
                    disabled={
                      !enableResendOtp ||
                      resendAttempts === 0 ||
                      (timeLeft > 0 && resendAttempts === 3)
                    }
                    onClick={handleResendOtp}
                  >
                    {t("resend_otp")}
                  </Button>
                  {resendAttempts !==
                    settings.response.configs["resend.attempts"] && (
                    <ResendAttempt
                      currentAttempts={resendAttempts}
                      totalAttempts={
                        settings.response.configs["resend.attempts"]
                      }
                    />
                  )}
                  {resendAttempts === 0 && (
                    <Button
                      variant="link"
                      className="text-sm h-4 m-4"
                      onClick={handleExhaustedAttempt}
                    >
                      {t("go_back_to_landing_page")}
                    </Button>
                  )}
                </div>
              </>
            )}
          </div>
        </StepContent>
      </Step>
    </>
  );
};
