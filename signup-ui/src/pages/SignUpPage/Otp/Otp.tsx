import { useCallback, useEffect, useRef, useState } from "react";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { Trans, useTranslation } from "react-i18next";
import PinInput from "react-pin-input";
import { useSearchParams } from "react-router-dom";

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
import { maskPhoneNumber } from "~utils/phone";
import { convertTime } from "~utils/timer";
import {
  Error,
  GenerateChallengeRequestDto,
  SettingsDto,
  VerifyChallengeRequestDto,
} from "~typings/types";

import { useGenerateChallenge, useVerifyChallenge } from "../mutations";
import { SignUpForm } from "../SignUpPage";
import {
  setStepSelector,
  SignUpStep,
  stepSelector,
  useSignUpStore,
} from "../useSignUpStore";
import { ResendAttempt } from "./components/ResendAttempt";
import { useTimer } from "./hooks/useTimer";

interface OtpProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const Otp = ({ methods, settings }: OtpProps) => {
  const { t } = useTranslation();

  const [hasError, setHasError] = useState<boolean>(false);
  const pinInputRef = useRef<PinInput | null>(null);
  const { control, getValues, setValue } = useFormContext();
  const { step, setStep } = useSignUpStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        setStep: setStepSelector(state),
      }),
      []
    )
  );
  const { trigger, reset, formState } = methods;
  const [resendAttempts, setResendAttempts] = useState<number>(0);
  const [enableResendOtp, setEnableResendOtp] = useState<boolean>(false);
  const [timeLeft, setTimeLeft] = useTimer(0, setEnableResendOtp);
  const { generateChallengeMutation } = useGenerateChallenge();
  const { verifyChallengeMutation } = useVerifyChallenge();
  const [error, setError] = useState<Error | null>(null);
  const [showDialog, setShowDialog] = useState(false);
  const [searchParams] = useSearchParams();

  useEffect(() => {
    setTimeLeft(settings.response.configs["resend.delay"]);
    setResendAttempts(settings.response.configs["resend.attempts"]);
  }, [settings.response.configs, setTimeLeft]);

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
            pinInputRef.current?.clear();
            setValue("otp", "", { shouldValidate: true });

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
      setValue,
      setTimeLeft,
    ]
  );

  const handleBack = useCallback(() => {
    if (step === SignUpStep.Otp)
      setValue("captchaToken", "", { shouldValidate: true });

    setStep(SignUpStep.Phone);
  }, [step, setStep, setValue]);

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
              setStep(SignUpStep.PhoneStatus);
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
    [setStep, trigger, getValues, verifyChallengeMutation]
  );

  const handleErrorRedirect = () => {
    const esignetLoginPage = searchParams.get("callback");
    if (error?.errorCode === "already-registered" && esignetLoginPage) {
      window.location.href = esignetLoginPage;
    } else {
      setStep(SignUpStep.Phone);
      reset();
    }
  };

  const handleExhaustedAttempt = () => {
    setStep(SignUpStep.Phone);
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
              searchParams.get("callback")
                ? t("login")
                : t("okay")}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
      <Step>
        <StepHeader className="px-0 py-9">
          <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-base font-semibold">
            <Icons.back
              className="absolute left-0 ml-8 cursor-pointer"
              onClick={handleBack}
            />
            <div className="w-full text-center text-[26px] font-semibold">
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
              className="h-4 w-4 cursor-pointer text-destructive"
              onClick={() => setHasError(false)}
            />
          </div>
          {/* OTP inputs */}
          <div className="flex flex-col gap-y-6 p-6">
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
                      secretDelay={200}
                      focus
                      initialValue={field.value}
                      type="numeric"
                      inputMode="number"
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        padding: "5px 0px",
                        fontSize: "24px",
                      }}
                      inputStyle={{
                        width: "55px",
                        height: "52px",
                        margin: "0px 0px",
                        border: "2px solid #C1C1C1",
                        color: "#000000",
                        borderRadius: "8px",
                      }}
                      inputFocusStyle={{
                        border: "2px solid #676766",
                      }}
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
              isLoading={verifyChallengeMutation.isPending}
            >
              {t("verify")}
            </Button>
            <div className="mx-12 flex flex-col items-center">
              <div className="flex gap-x-1 text-center">
                <Trans
                  i18nKey="resend_otp_detail"
                  components={{
                    CountDownSpan: <span className="font-semibold" />,
                  }}
                  values={{ countDown: convertTime(timeLeft) }}
                />
              </div>
              <Button
                variant="link"
                className="m-1 h-5 text-base font-bold"
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
                  totalAttempts={settings.response.configs["resend.attempts"]}
                />
              )}
              {resendAttempts === 0 && (
                <Button
                  variant="link"
                  className="m-4 h-4 text-sm"
                  onClick={handleExhaustedAttempt}
                >
                  {t("go_back_to_landing_page")}
                </Button>
              )}
            </div>
          </div>
        </StepContent>
      </Step>
    </>
  );
};
