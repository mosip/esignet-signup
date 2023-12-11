import { useCallback } from "react";
import { useMutationState } from "@tanstack/react-query";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent } from "~components/ui/step";
import { getSignInRedirectURL } from "~utils/link";
import { keys as mutationKeys } from "~pages/SignUpPage/mutations";
import { VerifyChallengeResponseDto } from "~typings/types";

import { SignUpForm } from "../SignUpPage";
import { setStepSelector, SignUpStep, useSignUpStore } from "../useSignUpStore";

interface PhoneStatusProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const PhoneStatus = ({ methods }: PhoneStatusProps) => {
  const { t } = useTranslation();

  const { setStep } = useSignUpStore(
    useCallback((state) => ({ setStep: setStepSelector(state) }), [])
  );
  const { hash: fromSignInHash } = useLocation();
  const { trigger } = methods;

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();
      const isStepValid = await trigger();

      if (isStepValid) {
        setStep(SignUpStep.AccountSetup);
      }
    },
    [trigger, setStep]
  );

  const handleChallengeVerificationErrorRedirect = (e: any) => {
    e.preventDefault();
    window.location.href = getSignInRedirectURL(fromSignInHash);
  };

  const [challengeVerification] = useMutationState<VerifyChallengeResponseDto>({
    filters: {
      mutationKey: mutationKeys.challengeVerification,
      status: "success",
    },
    select: (mutation) => mutation.state.data as VerifyChallengeResponseDto,
  });

  if (
    challengeVerification.errors.length > 0 &&
    challengeVerification.errors[0].errorCode === "already-registered"
  ) {
    return (
      <Step>
        <StepContent>
          <div className="flex flex-col items-center gap-4 px-4">
            <FailedIconSvg />
            <h1 className="text-center text-2xl font-semibold">
              {t("signup_failed")}
            </h1>
            <p className="text-center text-gray-500">
              {t("mobile_number_already_registered")}
            </p>
          </div>
          <Button
            className="my-4 h-16 w-full"
            onClick={handleChallengeVerificationErrorRedirect}
          >
            {fromSignInHash ? t("login") : t("okay")}
          </Button>
        </StepContent>
      </Step>
    );
  }

  return (
    <Step>
      <StepContent>
        <div className="flex flex-col items-center gap-4 px-4">
          <SuccessIconSvg />
          <h1 className="text-center text-2xl font-semibold">
            {t("successful")}
          </h1>
          <p className="text-center text-gray-500">
            {t("mobile_number_verified")}
          </p>
        </div>
        <Button className="my-4 h-16 w-full" onClick={handleContinue}>
          {t("continue")}
        </Button>
      </StepContent>
    </Step>
  );
};
