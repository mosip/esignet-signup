import { useCallback, useEffect } from "react";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { RegistrationWithFailedStatus, SettingsDto } from "~typings/types";

import { useRegistrationStatus } from "../queries";
import { SignUpForm } from "../SignUpPage";
import { setStepSelector, SignUpStep, useSignUpStore } from "../useSignUpStore";

interface AccountSetupStatusProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountSetupStatus = ({
  methods,
  settings,
}: AccountSetupStatusProps) => {
  const { t } = useTranslation();

  const { setStep } = useSignUpStore(
    useCallback((state) => ({ setStep: setStepSelector(state) }), [])
  );
  const { trigger } = methods;

  const { data: registrationStatus, isError } = useRegistrationStatus(
    settings.response.configs["status.request.limit"],
    settings.response.configs["status.request.delay"]
  );

  useEffect(() => {
    if (isError) {
      // TODO: handle case the request limit is reached
    }

    if (
      registrationStatus?.response.status ===
      RegistrationWithFailedStatus.FAILED
    ) {
      // TODO: handle case the registration status failed
    }
    if (
      registrationStatus?.response.status ===
      RegistrationWithFailedStatus.COMPLETED
    ) {
      setStep(SignUpStep.AccountRegistrationStatus);
    }
  }, [registrationStatus, settings.response.configs, isError, setStep]);

  return (
    <Step>
      <StepContent className="py-16">
        <div className="flex flex-col items-center gap-8">
          <Icons.loader className="h-20 w-20 animate-spin text-orange-500" />
          <div>
            <h1 className="text-center text-2xl font-semibold">
              {t("setup_progress")}
            </h1>
            <p className="text-center text-gray-500">
              {t("setup_progress_wait")}
            </p>
          </div>
        </div>
      </StepContent>
    </Step>
  );
};
