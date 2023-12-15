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
import { Popover, PopoverArrow, PopoverContent } from "~components/ui/popover";
import {
  Step,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { cn } from "~utils/cn";
import { RegistrationRequestDto, SettingsDto } from "~typings/types";

import { useRegister } from "../mutations";
import { SignUpForm } from "../SignUpPage";
import {
  setCriticalErrorSelector,
  setStepSelector,
  SignUpStep,
  useSignUpStore,
} from "../useSignUpStore";
import { TermsAndPrivacyModal } from "./components/TermsAndPrivacyModal";

interface AccountSetupProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountSetup = ({ settings, methods }: AccountSetupProps) => {
  const { t } = useTranslation();

  const { setStep, setCriticalError } = useSignUpStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );
  const {
    control,
    trigger,
    getValues,
    reset,
    formState: { errors: formErrors, isValid },
  } = methods;

  const { registerMutation } = useRegister();
  const [openTermConditionModal, setOpenTermConditionModal] = useState(false);
  const [modalData, setModalData] = useState({title: "", content: ""});

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();
      const isStepValid = await trigger();

      if (isStepValid) {
        const RegistrationRequestDto: RegistrationRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            username: `${
              settings.response.configs["identifier.prefix"]
            }${getValues("phone")}`,
            password: getValues("password"),
            consent: getValues("consent") ? "AGREE" : "DISAGREE",
            userInfo: {
              fullName: [
                { language: "khm", value: getValues("fullNameInKhmer") },
              ],
              phone: `${
                settings.response.configs["identifier.prefix"]
              }${getValues("phone")}`,
              preferredLang: "eng",
            },
          },
        };

        return registerMutation.mutate(RegistrationRequestDto, {
          onSuccess: ({ errors }) => {
            if (
              errors.length > 0 &&
              errors[0].errorCode === "invalid_transaction"
            ) {
              setCriticalError(errors[0]);
            } else {
              setStep(SignUpStep.AccountSetupStatus);
            }
          },
        });
      }
    },
    [setStep, trigger, getValues, registerMutation]
  );

  const onModalToggle = () => {
    setOpenTermConditionModal(false);
    setModalData({title: "", content: ""});
  }

  const onOpenTerm = (e: any) => {
    e.preventDefault();
    setModalData({title: t("terms_and_conditions_title"), content: t("terms_and_conditions_content")});
    setOpenTermConditionModal(true);
  }

  const onOpenPrivacy = (e: any) => {
    e.preventDefault();
    setModalData({title: t("privacy_and_policy_title"), content: t("privacy_and_policy_content")});
    setOpenTermConditionModal(true);
  }

  return (
    <div className="my-10 sm:my-0">
      <Step>
        <StepHeader>
          <StepTitle>{t("setup_account")}</StepTitle>
          <StepDescription>{t("complete_your_registration")}</StepDescription>
        </StepHeader>
        <StepDivider />
        <StepContent className="px-10 py-8 sm:px-[18px] sm:pt-[22px] sm:pb-[70px]">
          <div className="flex flex-col gap-y-6">
            <FormField
              control={control}
              name="username"
              render={({ field }) => (
                <FormItem className="space-y-0">
                  <div className="space-y-2">
                    <FormLabel>{t("username")}</FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t("username_placeholder")}
                        {...field}
                        value={`${
                          settings.response.configs["identifier.prefix"]
                        } ${getValues("phone")}`}
                        className="py-6"
                        disabled
                      />
                    </FormControl>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="fullNameInKhmer"
              render={({ field }) => (
                <FormItem className="space-y-0">
                  <div className="space-y-2">
                    <div className="flex items-center gap-1">
                      <FormLabel>{t("full_name")}</FormLabel>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Icons.info className="h-4 w-4 cursor-pointer" />
                        </PopoverTrigger>
                        <PopoverContent side="right">
                          {t("full_name_tooltip")}
                          <PopoverArrow className="fill-[#FFFFFF] stroke-[#BCBCBC]" />
                        </PopoverContent>
                      </Popover>
                    </div>
                    <FormControl>
                      <Input
                        placeholder={t("full_name_placeholder")}
                        className={cn(
                          "h-[52px] py-6",
                          formErrors.fullNameInKhmer && "border-destructive"
                        )}
                        {...field}
                      />
                    </FormControl>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="password"
              render={({ field }) => (
                <FormItem className="space-y-0">
                  <div className="space-y-2">
                    <div className="flex items-center gap-1">
                      <FormLabel>{t("password")}</FormLabel>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Icons.info className="h-4 w-4 cursor-pointer" />
                        </PopoverTrigger>
                        <PopoverContent side="right" className="w-full">
                          <Trans
                            i18nKey="password_rules"
                            components={{
                              ul: <ul className="list-inside list-disc" />,
                              li: <li />,
                            }}
                          />
                          <PopoverArrow className="fill-[#FFFFFF] stroke-[#BCBCBC]" />
                        </PopoverContent>
                      </Popover>
                    </div>
                    <FormControl>
                      <Input
                        type="password"
                        placeholder={t("password_placeholder")}
                        className={cn(
                          "h-[52px] py-6",
                          formErrors.password && "border-destructive"
                        )}
                        {...field}
                      />
                    </FormControl>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="confirmPassword"
              render={({ field }) => (
                <FormItem className="space-y-0">
                  <div className="space-y-2">
                    <FormLabel>{t("confirm_password")}</FormLabel>
                    <FormControl>
                      <Input
                        type="password"
                        placeholder={t("confirm_password_placeholder")}
                        className={cn(
                          "h-[52px] py-6",
                          formErrors.confirmPassword && "border-destructive"
                        )}
                        {...field}
                      />
                    </FormControl>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="consent"
              render={({ field }) => (
                <FormItem className="flex items-start gap-x-4 space-y-0">
                  <FormControl>
                    <Checkbox
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      className="h-5 w-5 rounded-[2px] text-white data-[state=checked]:border-primary data-[state=checked]:bg-primary"
                    />
                  </FormControl>
                  <FormLabel className="font-medium">
                    <Trans
                      i18nKey="terms_and_condition"
                      components={{
                        TermsAndConditionsAnchor: (
                          <a
                            href="#!"
                            className="text-primary underline"
                            target="_blank"
                            aria-label="Terms and Conditions"
                            onClick={onOpenTerm}
                          />
                        ),
                        PrivacyPolicyAnchor: (
                          <a
                            href="#!"
                            className="text-primary underline"
                            target="_blank"
                            aria-label="Terms and Conditions"
                            onClick={onOpenPrivacy}
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
              className="w-full"
              onClick={handleContinue}
              disabled={!isValid}
              isLoading={registerMutation.isPending}
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
    </div>
  );
};
