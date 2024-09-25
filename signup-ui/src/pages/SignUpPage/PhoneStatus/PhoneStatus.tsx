import { useCallback } from "react";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent } from "~components/ui/step";
import { getSignInRedirectURL } from "~utils/link";
import { useSettings } from "~pages/shared/queries";

import { SignUpForm } from "../SignUpPage";
import {
  setStepSelector,
  SignUpStep,
  useSignUpStore,
  verificationChallengeErrorSelector,
} from "../useSignUpStore";

interface PhoneStatusProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const PhoneStatus = ({ methods }: PhoneStatusProps) => {
  const { t } = useTranslation();
  const { data: settings } = useSettings();

  const { setStep, challengeVerification } = useSignUpStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
        challengeVerification: verificationChallengeErrorSelector(state),
      }),
      []
    )
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
    window.location.href = getSignInRedirectURL(
      settings?.response.configs["signin.redirect-url"],
      fromSignInHash,
      "/signup"
    );
  };

  if (
    challengeVerification &&
    ["already-registered", "identifier_already_registered"].includes(
      challengeVerification.errorCode
    )
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
            id="signup-failed-okay-button"
            name="signup-failed-okay-button"
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
        <Button
          id="mobile-number-verified-continue-button"
          name="mobile-number-verified-continue-button"
          className="my-4 h-16 w-full"
          onClick={handleContinue}
        >
          {t("continue")}
        </Button>
      </StepContent>
    </Step>
  );
};
