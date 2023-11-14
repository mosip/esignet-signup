import { useCallback } from "react";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";

import StatusPageTemplate from "~templates/ResponsePageTemplate";

import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";

interface StatusProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const Status = ({ methods }: StatusProps) => {
  const { t } = useTranslation();

  const { setActiveStep } = useSignUpContext();
  const { trigger } = methods;

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();
      const isStepValid = await trigger();

      if (isStepValid) {
        setActiveStep((prevActiveStep) => prevActiveStep + 1);
      }
    },
    [trigger, setActiveStep]
  );

  return (
    <StatusPageTemplate
      title={t("successful")}
      description={t("mobile_number_verified")}
      action={t("continue")}
      status="success"
      handleAction={handleContinue}
    />
  );
};
