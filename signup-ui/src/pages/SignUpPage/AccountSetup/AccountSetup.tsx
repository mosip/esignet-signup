import { KeyboardEvent, useCallback, useMemo, useState } from "react";
import { UseFormReturn } from "react-hook-form";
import { Trans, useTranslation } from "react-i18next";

import { IconLabelPopover } from "~components/icon-label-popover";
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
import {
  Step,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { cn } from "~utils/cn";
import { handleInputFilter } from "~utils/input";
import { useRegister } from "~pages/shared/mutations";
import { RegistrationRequestDto, SettingsDto } from "~typings/types";

import { SignUpForm, signUpFormDefaultValues } from "../SignUpPage";
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
    formState: { errors: formErrors, isValid, isDirty },
  } = methods;

  const { registerMutation } = useRegister();
  const [openTermConditionModal, setOpenTermConditionModal] = useState(false);
  const [modalData, setModalData] = useState({ title: "", content: "" });

  const disabledContinue =
    !isValid ||
    !isDirty ||
    getValues("fullNameInKhmer") === signUpFormDefaultValues.fullNameInKhmer ||
    getValues("password") === signUpFormDefaultValues.password ||
    getValues("confirmPassword") === signUpFormDefaultValues.confirmPassword ||
    getValues("consent") === signUpFormDefaultValues.consent;

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();

      if (registerMutation.isPending) return;

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
            locale: null,
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

  const handleFullNameInput = (event: KeyboardEvent<HTMLInputElement>) =>
    handleInputFilter(
      event,
      settings.response.configs["fullname.allowed.characters"]
    );

  return (
    <div className="my-10 sm:my-0">
      <Step>
        <StepHeader>
          <StepTitle>{t("setup_account")}</StepTitle>
          <StepDescription>{t("complete_your_registration")}</StepDescription>
        </StepHeader>
        <StepDivider />
        <StepContent className="px-10 py-8 sm:px-[18px] sm:pb-[70px] sm:pt-[22px]">
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
                        id="username"
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
                      <IconLabelPopover
                        icon={
                          <Icons.info
                            id="fullName-info-icon"
                            className="m-1 h-4 w-4 cursor-pointer sm:h-3 sm:w-3"
                            alt="info icon"
                          />
                        }
                      >
                        <span>{t("full_name_tooltip")}</span>
                      </IconLabelPopover>
                    </div>
                    <FormControl>
                      <Input
                        id="fullNameInKhmer"
                        {...field}
                        className={cn(
                          "h-[52px] py-6",
                          formErrors.fullNameInKhmer && "border-destructive"
                        )}
                        placeholder={t("full_name_placeholder")}
                        minLength={
                          settings.response.configs["fullname.length.min"]
                        }
                        maxLength={
                          settings.response.configs["fullname.length.max"]
                        }
                        onKeyDown={handleFullNameInput}
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
                      <IconLabelPopover
                        icon={
                          <Icons.info
                            id="password-info-icon"
                            className="m-1 h-4 w-4 cursor-pointer sm:h-3 sm:w-3"
                            alt="info icon"
                          />
                        }
                      >
                        <Trans
                          i18nKey="password_rules"
                          components={{
                            ul: <ul className="list-inside list-disc" />,
                            li: <li />,
                          }}
                        />
                      </IconLabelPopover>
                    </div>
                    <FormControl>
                      <Input
                        id="password"
                        {...field}
                        type="password"
                        placeholder={t("password_placeholder")}
                        className={cn(
                          "h-[52px] py-6",
                          formErrors.password && "border-destructive"
                        )}
                        minLength={
                          settings.response.configs["password.length.min"]
                        }
                        maxLength={
                          settings.response.configs["password.length.max"]
                        }
                        onBlur={() => {
                          trigger("password");
                          trigger("confirmPassword");
                        }}
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
                        id="confirmPassword"
                        {...field}
                        type="password"
                        placeholder={t("confirm_password_placeholder")}
                        className={cn(
                          "h-[52px] py-6",
                          formErrors.confirmPassword && "border-destructive"
                        )}
                        minLength={
                          settings.response.configs["password.length.min"]
                        }
                        maxLength={
                          settings.response.configs["password.length.max"]
                        }
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
                      id="consent-button"
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
                          <span
                            className="text-primary underline hover:cursor-pointer"
                            aria-label="Terms and Conditions"
                            onClick={onOpenTerm}
                          />
                        ),
                        PrivacyPolicyAnchor: (
                          <span
                            className="text-primary underline hover:cursor-pointer"
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
              id="account-setup-submit-button"
              name="account-setup-submit-button"
              type="submit"
              className="w-full"
              onClick={handleContinue}
              disabled={disabledContinue}
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
