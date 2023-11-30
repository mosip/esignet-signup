import { useEffect, useState } from "react";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { Icons } from "~components/ui/icons";
import { Step, StepContent, StepHeader, StepTitle } from "~components/ui/step";
import { RegistrationWithFailedStatus, SettingsDto } from "~typings/types";

import { useRegistrationStatus } from "../queries";
import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";

interface StatusProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountRegistrationStatus = ({
  methods,
  settings,
}: StatusProps) => {
  const { t } = useTranslation();

  const { setActiveStep } = useSignUpContext();
  const { trigger } = methods;

  const { data: registrationStatus, isError } = useRegistrationStatus(
    settings.response.configs["status.request.limit"],
    settings.response.configs["status.request.delay"]
  );

  useEffect(() => {
    if (isError) {
      setActiveStep((prevActiveStep) => prevActiveStep + 1);
    }

    if (
      registrationStatus?.response.status ===
      RegistrationWithFailedStatus.FAILED
    ) {
      setActiveStep((prevActiveStep) => prevActiveStep + 1);
    }
    if (
      registrationStatus?.response.status ===
      RegistrationWithFailedStatus.COMPLETED
    ) {
      setActiveStep((prevActiveStep) => prevActiveStep + 1);
    }
  }, [registrationStatus, settings.response.configs, isError, setActiveStep]);

  return (
    <Step>
      <div className="p-16">
        <StepHeader>
          <StepTitle>
            <Icons.loader className="h-20 w-20 animate-spin text-orange-500" />
          </StepTitle>
        </StepHeader>
        <StepContent>
          <div className="flex flex-col items-center gap-4 px-4">
            <h1 className="text-center text-2xl font-semibold">
              {t("setup_progress")}
            </h1>
            <p className="text-center text-sm text-gray-500">
              {t("setup_progress_wait")}
            </p>
          </div>
        </StepContent>
      </div>
    </Step>
  );
};
