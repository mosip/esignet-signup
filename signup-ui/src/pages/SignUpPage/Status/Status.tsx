import { useCallback } from "react";
import { UseFormReturn } from "react-hook-form";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import StatusPageTemplate from "~templates/ResponsePageTemplate";

import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";

interface StatusProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const Status = ({ methods }: StatusProps) => {
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
      title="Successful!"
      description="Your mobile number has been verified successfully. Please continue to setup your account and complete the registration process."
      action="Continue"
      status="success"
      handleAction={handleContinue}
    />
  );
};
