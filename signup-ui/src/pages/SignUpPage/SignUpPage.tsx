import { useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { isValidPhoneNumber } from "libphonenumber-js";
import { Resolver, useForm } from "react-hook-form";
import * as yup from "yup";

import { Form } from "~components/ui/form";
import { SettingsDto } from "~typings/types";

import AccountSetup from "./AccountSetup";
import Otp from "./Otp";
import Phone from "./Phone";
import RegistrationStatus from "./RegistrationStatus";
import { useSignUpContext } from "./SignUpContext";
import Status from "./Status";

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

export enum SignUpSteps {
  PHONE = "PHONE",
  OTP = "OTP",
  STATUS = "STATUS",
  ACCOUNTSETUP = "ACCOUNTSETUP",
}

interface SignUpPageProps {
  settings: SettingsDto;
}

export const SignUpPage = ({ settings }: SignUpPageProps) => {
  const { activeStep } = useSignUpContext();
  const steps = Object.values(SignUpSteps);

  const validationSchema = useMemo(() => {
    return [
      // Step 1 - Phone Validation
      yup.object({
        phone: yup
          .string()
          .required()
          .min(1, "Failed to send OTP. Please provide a valid mobile number.")
          .test(
            "is-phone-number",
            "Failed to send OTP. Please provide a valid mobile number.",
            (phone) => isValidPhoneNumber(phone, "KH")
          ),
        captchaToken: yup
          .string()
          .required("Please verify that you are a human."),
      }),
      // Step 2 - OTP Validation
      yup.object({
        otp: yup.string().matches(/^\d{6}$/gm),
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
            "Please enter a valid name"
          ),
        password: yup
          .string()
          .matches(
            new RegExp(settings.response.configs["password.pattern"]),
            "Please enter a valid password"
          ),
        confirmPassword: yup
          .string()
          .matches(
            new RegExp(settings.response.configs["password.pattern"]),
            "Please enter a valid password"
          )
          .oneOf([yup.ref("password")], "Passwords must match"),
        consent: yup
          .bool()
          .oneOf([true], "You must accept the terms and conditions"),
      }),
      // Step 5 - Register Status Validation
      yup.object({}),
    ];
  }, [settings]);

  const currentValidationSchema = validationSchema[activeStep];

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

  const getSignUpStepContent = (step: number) => {
    switch (step) {
      case 0:
        return <Phone methods={methods} />;
      case 1:
        return <Otp methods={methods} />;
      case 2:
        return <Status methods={methods} />;
      case 3:
        return <AccountSetup methods={methods} />;
      default:
        return "unknown step";
    }
  };

  return (
    <>
      {activeStep === steps.length ? (
        <RegistrationStatus />
      ) : (
        <Form {...methods}>
          <form>{getSignUpStepContent(activeStep)}</form>
        </Form>
      )}
    </>
  );
};
