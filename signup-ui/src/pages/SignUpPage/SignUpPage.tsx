import { zodResolver } from "@hookform/resolvers/zod";
import { isValidPhoneNumber } from "libphonenumber-js";
import { FormProvider, useForm } from "react-hook-form";
import * as z from "zod";

import { Button } from "~components/ui/button";

import OTP from "./Otp";
import Phone from "./Phone";
import { useSettings } from "./queries";
import { useSignUpContext } from "./SignUpContext";
import Status from "./Status";

const validationSchema = [
  // Phone validation
  z.object({
    phone: z
      .string()
      .min(1, "Failed to send OTP. Please provide a valid mobile number.")
      .refine(
        (phone) => isValidPhoneNumber(phone, "KH"),
        "Failed to send OTP. Please provide a valid mobile number."
      ),
    captchaToken: z.string().nonempty("Please verify that you are a human."),
  }),
  // OTP validation
  z.object({
    otp: z.string().regex(/^\d{6}$/gm),
  }),
];

export interface SignUpForm {
  phone: string;
  captchaToken: string;
  otp: string;
}

export enum SignUpSteps {
  PHONE = "PHONE",
  OTP = "OTP",
  STATUS = "STATUS",
}

export const SignUpPage = () => {
  const { data: settings, isLoading } = useSettings();
  const { activeStep, setActiveStep } = useSignUpContext();
  const steps = Object.values(SignUpSteps);

  const currentValidationSchema = validationSchema[activeStep];

  const signUpFormDefaultValues: SignUpForm = {
    phone: "",
    captchaToken: "",
    otp: "",
  };

  const methods = useForm({
    shouldUnregister: false,
    defaultValues: signUpFormDefaultValues,
    resolver: zodResolver(currentValidationSchema),
    mode: "onChange",
  });

  const { reset } = methods;

  const handleReset = () => {
    setActiveStep(0);
    reset();
  };

  const getSignUpStepContent = (step: number) => {
    switch (step) {
      case 0:
        return <Phone methods={methods} />;
      case 1:
        return <OTP methods={methods} />;
      case 2:
        return <Status />;
      default:
        return "unknown step";
    }
  };

  return (
    <div>
      {activeStep === steps.length ? (
        <Button onClick={handleReset}>reset</Button>
      ) : (
        <FormProvider {...methods}>
          <form>
            <div className="h-screen flex items-center">
              {isLoading && <div>Loading</div>}
              {settings && <>{getSignUpStepContent(activeStep)}</>}
            </div>
          </form>
        </FormProvider>
      )}
    </div>
  );
};
