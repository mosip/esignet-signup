import { useCallback, useState } from "react";
import { PopoverTrigger } from "@radix-ui/react-popover";
import { UseFormReturn } from "react-hook-form";
import { Trans, useTranslation } from "react-i18next";

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
  const { t } = useTranslation();

  const { setActiveStep } = useSignUpContext();
  const { control, trigger, getValues, formState } = methods;

  const { registerMutation } = useRegister();
  const [openTermConditionModal, setOpenTermConditionModal] = useState(false);
  const [modalData, setModalData] = useState({ title: "", content: "" });

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
    setOpenTermConditionModal(false);
    setModalData({ title: "", content: "" });
  };

  const onOpenTerm = (e: any) => {
    e.preventDefault();
    setModalData({
      title: t("terms_and_conditions_title"),
      content: t("terms_and_conditions_content"),
    });
    setOpenTermConditionModal(true);
  };

  const onOpenPrivacy = (e: any) => {
    e.preventDefault();
    setModalData({
      title: t("privacy_and_policy_title"),
      content: t("privacy_and_policy_content"),
    });
    setOpenTermConditionModal(true);
  };

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
                    <FormLabel>{t("username")}</FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t("username_placeholder")}
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
                      <FormLabel>{t("full_name")}</FormLabel>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Icons.info className="w-4 h-4 cursor-pointer" />
                        </PopoverTrigger>
                        <PopoverContent side="right">
                          {t("full_name_tooltip")}
                        </PopoverContent>
                      </Popover>
                    </div>
                    <FormControl>
                      <Input
                        placeholder={t("full_name_placeholder")}
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
                      <FormLabel>{t("password")}</FormLabel>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Icons.info className="w-4 h-4 cursor-pointer" />
                        </PopoverTrigger>
                        <PopoverContent side="right" className="w-80">
                          <div className="flex items-center justify-center">
                            <ul className="list-disc">
                              <Trans
                                i18nKey="password_rules"
                                components={{
                                  li: <li />,
                                }}
                              />
                            </ul>
                          </div>
                        </PopoverContent>
                      </Popover>
                    </div>
                    <FormControl>
                      <Input
                        type="password"
                        placeholder={t("password_placeholder")}
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
                    <FormLabel>{t("confirm_password")}</FormLabel>
                    <FormControl>
                      <Input
                        type="password"
                        placeholder={t("confirm_password_placeholder")}
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
                    <FormControl>
                      <Checkbox
                        checked={field.value}
                        onCheckedChange={field.onChange}
                        className="h-5 w-5 rounded-[2px] text-white data-[state=checked]:bg-orange-500 data-[state=checked]:border-orange-500"
                      />
                    </FormControl>
                    <FormLabel>
                      <Trans
                        i18nKey="terms_and_condition"
                        components={{
                          TermsAndConditionsAnchor: (
                            <a
                              href="#!"
                              className="text-orange-500 underline"
                              target="_blank"
                              aria-label="Terms and Conditions"
                            />
                          ),
                          PrivacyPolicyAnchor: (
                            <a
                              href="#!"
                              className="text-orange-500 underline"
                              target="_blank"
                              aria-label="Terms and Conditions"
                            />
                          ),
                        }}
                      />
                    </FormLabel>
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
                {t("continue")}
              </Button>

              <TermsAndPrivacyModal
                title={modalData.title}
                content={modalData.content}
                isOpen={openTermConditionModal}
                backdrop="static"
                toggleModal={onModalToggle}
              />
            </div>
          </StepContent>
        </Step>
      )}
    </>
  );
};
