import { useCallback, useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { AsYouType, isValidPhoneNumber } from "libphonenumber-js";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";

import { Form } from "~components/ui/form";
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

  const validationSchema = useMemo(() => {
    return [
      // Step 1 - Phone Validation
      yup.object({
        phone: yup
          .string()
          .required(t("username_validation"))
          .matches(/^[^0].*$/, t("username_validation"))
          .matches(
            new RegExp(settings.response.configs["identifier.pattern"]),
            t("username_validation")
          ),
        captchaToken: yup.string().required(t("captcha_token_validation")),
      }),
      // Step 2 - OTP Validation
      yup.object({
        otp: yup
          .string()
          .matches(
            new RegExp(`^\\d{${settings.response.configs["otp.length"]}}$`)
          ),
      }),
      // Step 3 - Status Validation
      yup.object({}),
      // Step 4 - Account Setup Validation
      yup.object({
        username: yup.string(),
        fullNameInKhmer: yup
          .string()
          .matches(
            new RegExp(settings.response.configs["fullname.pattern"]),
            t("full_name_validation")
          ),
        password: yup
          .string()
          .matches(
            new RegExp(settings.response.configs["password.pattern"]),
            t("password_validation")
          ),
        confirmPassword: yup
          .string()
          .matches(
            new RegExp(settings.response.configs["password.pattern"]),
            t("password_validation")
          )
          .oneOf([yup.ref("password")], t("password_validation_must_match")),
        consent: yup.bool().oneOf([true], t("terms_and_conditions_validation")),
      }),
      // Step 5 - Register Status Validation
      yup.object({}),
      yup.object({}),
    ];
  }, [settings, t]);

  const currentValidationSchema = validationSchema[step];

  const signUpFormDefaultValues: SignUpForm = {
    phone: "",
    captchaToken: "",
    otp: "",
    username: "",
    fullNameInKhmer: "",
    password: "",
    confirmPassword: "",
    consent: false,
  };

  const methods = useForm<SignUpForm>({
    shouldUnregister: false,
    defaultValues: signUpFormDefaultValues,
    resolver: yupResolver(currentValidationSchema) as unknown as Resolver<
      SignUpForm,
      any
    >,
    mode: "all",
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
