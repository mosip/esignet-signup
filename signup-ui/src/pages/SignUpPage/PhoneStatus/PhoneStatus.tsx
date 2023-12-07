import { useCallback } from "react";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent } from "~components/ui/step";

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
