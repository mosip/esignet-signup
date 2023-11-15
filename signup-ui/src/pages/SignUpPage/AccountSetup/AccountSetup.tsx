import { useCallback, useState } from "react";
import { PopoverTrigger } from "@radix-ui/react-popover";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import { Checkbox } from "~components/ui/checkbox";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import { Input } from "~components/ui/input";
import { Popover, PopoverContent } from "~components/ui/popover";
import {
  Step,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { RegisterRequestDto } from "~typings/types";

import { useRegister } from "../mutations";
import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";
import { AccountSetupProgress } from "./components/AccountSetupProgress";
import { TermsAndPrivacyModal } from "./components/TermsAndPrivacyModal";

interface AccountSetupProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountSetup = ({ methods }: AccountSetupProps) => {
  const {t} = useTranslation();
  const { setActiveStep } = useSignUpContext();
  const { control, trigger, getValues, formState } = methods;
  const { registerMutation } = useRegister();
  const [openTermCondition, setOpenTermCondition] = useState(false);
  const [modalData, setModalData] = useState({title: "", content: ""});

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();
      const isStepValid = await trigger();

      if (isStepValid) {
        const registerRequestDto: RegisterRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            username: `855${getValues("phone")}`,
            password: getValues("password"),
            consent: getValues("consent") ? "AGREE" : "DISAGREE",
            userInfo: {
              fullName: [
                { language: "khm", value: getValues("fullNameInKhmer") },
              ],
              phone: `+855${getValues("phone")}`,
              preferredLang: "eng",
            },
          },
        };

        return registerMutation.mutate(registerRequestDto, {
          onSuccess: ({ errors }) => {
            if (!errors) {
              setActiveStep((prevActiveStep) => prevActiveStep + 1);
            }
          },
        });
      }
    },
    [setActiveStep, trigger, getValues, registerMutation]
  );

  const onModalToggle = () => {
    setOpenTermCondition(false);
    setModalData({title: "", content: ""});
  }

  const onOpenTerm = (e: any) => {
    e.preventDefault();
    setModalData({title: t("terms_and_conditions_title"), content: t("terms_and_conditions_content")});
    setOpenTermCondition(true);
  }

  const onOpenPrivacy = (e: any) => {
    e.preventDefault();
    setModalData({title: t("privacy_and_policy_title"), content: t("privacy_and_policy_content")});
    setOpenTermCondition(true);
  }

  return (
    <>
      {registerMutation.isLoading && <AccountSetupProgress />}
      {!registerMutation.isLoading && (
        <Step>
          <StepHeader>
            <StepTitle>Setup Account</StepTitle>
            <StepDescription>
              Please enter the requested details to complete your registration.
            </StepDescription>
          </StepHeader>
          <StepDivider />
          <StepContent className="m-6">
            <div className="flex flex-col gap-y-4">
              <FormField
                control={control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Enter username"
                        {...field}
                        value={`+855 ${getValues("phone")}`}
                        disabled
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={control}
                name="fullNameInKhmer"
                render={({ field }) => (
                  <FormItem>
                    <div className="flex items-center gap-1">
                      <FormLabel>Full Name in Khmer</FormLabel>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Icons.info className="w-4 h-4 cursor-pointer" />
                        </PopoverTrigger>
                        <PopoverContent side="right">
                          Maximum 30 characters allowed and it should not
                          contain any digit or special characters except ""
                          space.
                        </PopoverContent>
                      </Popover>
                    </div>
                    <FormControl>
                      <Input
                        placeholder="Enter Full Name in Khmer"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <div className="flex items-center gap-1">
                      <FormLabel>Password</FormLabel>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Icons.info className="w-4 h-4 cursor-pointer" />
                        </PopoverTrigger>
                        <PopoverContent side="right" className="w-80">
                          <div className="flex items-center justify-center">
                            <ul className="list-disc">
                              <li>Require at least 8 characters long</li>
                              <li>Require at least 1 digit</li>
                              <li>Require at least 1 special character</li>
                              <li>Require at least 1 capital letter</li>
                            </ul>
                          </div>
                        </PopoverContent>
                      </Popover>
                    </div>
                    <FormControl>
                      <Input
                        type="password"
                        placeholder="Enter Password"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={control}
                name="confirmPassword"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Confirm Password</FormLabel>
                    <FormControl>
                      <Input
                        type="password"
                        placeholder="Enter Password"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={control}
                name="consent"
                render={({ field }) => (
                  <FormItem className="flex space-y-0 items-start gap-x-4">
                    <FormControl className="">
                      <Checkbox
                        checked={field.value}
                        onCheckedChange={field.onChange}
                        className="h-5 w-5 rounded-[2px] text-white data-[state=checked]:bg-orange-500 data-[state=checked]:border-orange-500"
                      />
                    </FormControl>
                    <FormLabel>
                      I agree to Cambodia's{" "}
                      <a className="text-orange-500 underline cursor-pointer" target="_blank" onClick={onOpenTerm}>
                        Terms & Conditions
                      </a>{" "}
                      and{" "}
                      <a className="text-orange-400 underline cursor-pointer" target="_blank" onClick={onOpenPrivacy}>
                        Privacy Policy
                      </a>
                      , to store & process my information as required.
                    </FormLabel>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <Button
                type="submit"
                variant="secondary"
                className="w-full"
                onClick={handleContinue}
                disabled={!formState.isValid}
              >
                Continue
              </Button>

              <TermsAndPrivacyModal
                title={modalData.title}
                content={modalData.content}
                isOpen={openTermCondition}
                backdrop="static"
                toggleModal={onModalToggle} />
            </div>
          </StepContent>
        </Step>
      )}
    </>
  );
};
