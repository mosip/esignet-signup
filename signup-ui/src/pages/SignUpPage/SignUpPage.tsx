import { useMemo } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { isValidPhoneNumber } from "libphonenumber-js";
import { useForm } from "react-hook-form";
import * as z from "zod";

import { Button } from "~components/ui/button";
import { Form } from "~components/ui/form";
import { SettingsDto } from "~typings/types";

import AccountSetup from "./AccountSetup";
import { AccountSetupProgress } from "./AccountSetup/components/AccountSetupProgress";
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
  const { activeStep, setActiveStep } = useSignUpContext();
  const steps = Object.values(SignUpSteps);

  const validationSchema = useMemo(() => {
    return [
      // Step 1 - Phone Validation
      z.object({
        phone: z
          .string()
          .min(1, "Failed to send OTP. Please provide a valid mobile number.")
          .refine(
            (phone) => isValidPhoneNumber(phone, "KH"),
            "Failed to send OTP. Please provide a valid mobile number."
          ),
        captchaToken: z
          .string()
          .nonempty("Please verify that you are a human."),
      }),
      // Step 2 - OTP Validation
      z.object({
        otp: z.string().regex(/^\d{6}$/gm),
      }),
      // Step 3 - Status Validation
      z.object({}),
      // Step 4 - Account Setup Validation
      z
        .object({
          username: z.string(),
          fullNameInKhmer: z
            .string()
            .regex(
              new RegExp(settings.response.configs["fullname.pattern"]),
              "Please enter a valid name"
            ),
          password: z
            .string()
            .regex(
              new RegExp(settings.response.configs["password.pattern"]),
              "Please enter a valid password"
            ),
          confirmPassword: z
            .string()
            .regex(
              new RegExp(settings.response.configs["password.pattern"]),
              "Please enter a valid password"
            ),
          consent: z.literal<boolean>(true),
        })
        .refine(
          ({ password, confirmPassword }) => password === confirmPassword,
          {
            path: ["confirmPassword"],
            message: "Password does not match",
          }
        ),
      // Step 5 - Register Status Validation
      z.object({}),
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

  const methods = useForm({
    shouldUnregister: false,
    defaultValues: signUpFormDefaultValues,
    resolver: zodResolver(currentValidationSchema),
    mode: "onChange",
  });

  const { reset } = methods;

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
    <div className="h-screen flex justify-center items-center">
      {activeStep === steps.length ? (
        // <Button onClick={handleReset}>reset</Button>
        <RegistrationStatus />
      ) : (
        <Form {...methods}>
          <form>{settings && getSignUpStepContent(activeStep)}</form>
        </Form>
      )}
    </div>
  );
};
