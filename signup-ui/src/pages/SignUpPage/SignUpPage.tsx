import { useCallback, useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";

import { Form } from "~components/ui/form";
import {
  validateCaptchaToken,
  validateConfirmPassword,
  validateFullName,
  validateOtp,
  validatePassword,
  validateUsername,
} from "~pages/shared/validation";
import { SettingsDto } from "~typings/types";

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
        phone: validateUsername(settings, t),
        captchaToken: validateCaptchaToken(t),
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
        fullNameInKhmer: validateFullName(settings, t),
        password: validatePassword(settings, t),
        confirmPassword: validateConfirmPassword("password", settings, t),
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
        ["invalid_transaction"].includes(criticalError.errorCode) && (
          <SignUpPopover />
        )}
      <Form {...methods}>
        <form>{getSignUpStepContent(step)}</form>
      </Form>
    </>
  );
};
