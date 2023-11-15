import { useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { isValidPhoneNumber } from "libphonenumber-js";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";

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
  const { t } = useTranslation();
  const { activeStep } = useSignUpContext();
  const steps = Object.values(SignUpSteps);

  const validationSchema = useMemo(() => {
    return [
      // Step 1 - Phone Validation
      yup.object({
        phone: yup
          .string()
          .required()
          .min(1, t("fail_to_send_otp"))
          .test("is-phone-number", t("fail_to_send_otp"), (phone) =>
            isValidPhoneNumber(phone, "KH")
          ),
        captchaToken: yup.string().required(t("captcha_token_validation")),
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
    ];
  }, [settings, t]);

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
        <FormProvider {...methods}>
          <form>
            <div className="grid grid-cols-12 gap-0">
              <div className="col-span-2">
                <img className="left-1 top-1" src="images/top.png" />
              </div>
              <div className="col-span-8 flex h-[calc(100vh-13.6vh)] items-center">
                {isLoading && <div>Loading</div>}
                {settings && <>{getSignUpStepContent(activeStep)}</>}
              </div>
              <div className="col-span-2 absolute bottom-0 right-0">
                <img className="" src="images/bottom.png" />
              </div>
            </div>
          </form>
        </FormProvider>
      )}
    </div>
  );
};
