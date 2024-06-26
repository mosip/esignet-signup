import { useCallback, useEffect, useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { useMutationState } from "@tanstack/react-query";
import { isEqual } from "lodash";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";

import { Button } from "~components/ui/button";
import { Form } from "~components/ui/form";
import { keys as mutationKeys } from "~pages/shared/mutations";
import {
  validateCaptchaToken,
  validateConfirmPassword,
  validateFullName,
  validateOtp,
  validatePassword,
  validateUsername,
} from "~pages/shared/validation";
import { SettingsDto, VerifyChallengeResponseDto } from "~typings/types";

import { AccountRegistrationStatus } from "./AccountRegistrationStatus/AccountRegistrationStatus";
import AccountSetup from "./AccountSetup";
import AccountSetupStatus from "./AccountSetupStatus";
import Otp from "./Otp";
import Phone from "./Phone";
import PhoneStatus from "./PhoneStatus";
import { SignUpPopover } from "./SignUpPopover";
import {
  criticalErrorSelector,
  SignUpStep,
  stepSelector,
  useSignUpStore,
} from "./useSignUpStore";

export interface SignUpForm {
  phone: string;
  captchaToken: string;
  otp: string;
  username: string;
  fullNameInKhmer: string;
  password: string;
  confirmPassword: string;
  consent: false;
}

export const signUpFormDefaultValues: SignUpForm = {
  phone: "",
  captchaToken: "",
  otp: "",
  username: "",
  fullNameInKhmer: "",
  password: "",
  confirmPassword: "",
  consent: false,
};

interface SignUpPageProps {
  settings: SettingsDto;
}

export const SignUpPage = ({ settings }: SignUpPageProps) => {
  const { t } = useTranslation();

  const { step, criticalError } = useSignUpStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        criticalError: criticalErrorSelector(state),
      }),
      []
    )
  );

  const validationSchema = useMemo(
    () => [
      // Step 1 - Phone Validation
      yup.object({
        phone: validateUsername(settings),
        captchaToken: validateCaptchaToken(settings),
      }),
      // Step 2 - OTP Validation
      yup.object({
        otp: validateOtp(settings),
      }),
      // Step 3 - Status Validation
      yup.object({}),
      // Step 4 - Account Setup Validation
      yup.object({
        username: yup.string(),
        fullNameInKhmer: validateFullName(settings),
        password: validatePassword(settings),
        confirmPassword: validateConfirmPassword("password", settings, true),
        consent: yup.bool().oneOf([true], t("terms_and_conditions_validation")),
      }),
      // Step 5 - Register Status Validation
      yup.object({}),
      yup.object({}),
    ],
    [settings, t]
  );

  const currentValidationSchema = validationSchema[step];

  const methods = useForm<SignUpForm>({
    shouldUnregister: false,
    defaultValues: signUpFormDefaultValues,
    resolver: yupResolver(currentValidationSchema) as unknown as Resolver<
      SignUpForm,
      any
    >,
    mode: "onBlur",
  });

  const { getValues } = methods;

  const [challengeVerification] = useMutationState<VerifyChallengeResponseDto>({
    filters: {
      mutationKey: mutationKeys.challengeVerification,
      status: "success",
    },
    select: (mutation) => mutation.state.data as VerifyChallengeResponseDto,
  });

  useEffect(() => {
    if (isEqual(signUpFormDefaultValues, getValues())) return;

    if (
      (step === SignUpStep.PhoneStatus &&
        challengeVerification.errors.length > 0 &&
        ["already-registered", "identifier_already_registered"].includes(
          challengeVerification.errors[0].errorCode
        )) ||
      step === SignUpStep.AccountRegistrationStatus ||
      (criticalError && criticalError.errorCode === "invalid_transaction")
    )
      return;

    const handleTabBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();

      return (event.returnValue = t("reset_password_discontinue_prompt"));
    };

    window.addEventListener("beforeunload", handleTabBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleTabBeforeUnload);
    };
  }, [step, criticalError, getValues()]);

  const getSignUpStepContent = (step: SignUpStep) => {
    switch (step) {
      case SignUpStep.Phone:
        return <Phone methods={methods} settings={settings} />;
      case SignUpStep.Otp:
        return <Otp methods={methods} settings={settings} />;
      case SignUpStep.PhoneStatus:
        return <PhoneStatus methods={methods} />;
      case SignUpStep.AccountSetup:
        return <AccountSetup methods={methods} settings={settings} />;
      case SignUpStep.AccountSetupStatus:
        return <AccountSetupStatus methods={methods} settings={settings} />;
      case SignUpStep.AccountRegistrationStatus:
        return <AccountRegistrationStatus />;
      default:
        return "unknown step";
    }
  };

  return (
    <>
      {criticalError &&
        ["invalid_transaction", "identifier_already_registered"].includes(
          criticalError.errorCode
        ) && <SignUpPopover />}
      <Form {...methods}>
        <form noValidate>{getSignUpStepContent(step)}</form>
      </Form>
    </>
  );
};
