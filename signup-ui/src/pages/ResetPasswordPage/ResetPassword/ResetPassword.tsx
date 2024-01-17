import { MouseEvent, useCallback, useMemo, useState } from "react";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { Trans, useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { LabelPopover } from "~components/label-popover";
import { ActionMessage } from "~components/ui/action-message";
import { Button } from "~components/ui/button";
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
  StepAlert,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { cn } from "~utils/cn";
import { useResetPassword } from "~pages/shared/mutations";
import {
  Error,
  ResetPasswordForm,
  ResetPasswordRequestDto,
  SettingsDto,
} from "~typings/types";

import { resetPasswordFormDefaultValues } from "../ResetPasswordPage";
import {
  ResetPasswordStep,
  setCriticalErrorSelector,
  setStepSelector,
  stepSelector,
  useResetPasswordStore,
} from "../useResetPasswordStore";

interface ResetPasswordProps {
  methods: UseFormReturn<ResetPasswordForm, any, undefined>;
  settings: SettingsDto;
}

export const ResetPassword = ({ methods, settings }: ResetPasswordProps) => {
  const { t } = useTranslation();

  const { control, setValue, getValues } = useFormContext();
  const [passwordResetError, setPasswordResetError] = useState<Error | null>(
    null
  );
  const { step, setStep, setCriticalError } = useResetPasswordStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );

  const {
    trigger,
    formState: {
      errors: passwordResetFormError,
      isValid: isResetPasswordValid,
      isDirty: isResetPasswordDirty,
    },
  } = methods;

  const { resetPasswordMutation } = useResetPassword();

  const handleBack = useCallback(() => {
    setValue("otp", "", { shouldValidate: true });
    setStep(ResetPasswordStep.Otp);
  }, [step, setStep, setValue]);

  const disabledContinue =
    !isResetPasswordValid ||
    !isResetPasswordDirty ||
    getValues("newPassword") === resetPasswordFormDefaultValues.newPassword ||
    getValues("confirmNewPassword") ===
      resetPasswordFormDefaultValues.confirmNewPassword;

  const handleContinue = useCallback(
    async (e: MouseEvent<HTMLButtonElement>) => {
      e.preventDefault();

      if (resetPasswordMutation.isPending) return;

      const isStepValid = await trigger();

      if (isStepValid) {
        const resetPasswordRequestDto: ResetPasswordRequestDto = {
          requestTime: new Date().toISOString(),
          request: {
            identifier: `${
              settings.response.configs["identifier.prefix"]
            }${getValues("username")}`,
            password: getValues("newPassword"),
          },
        };

        return resetPasswordMutation.mutate(resetPasswordRequestDto, {
          onSuccess: ({ errors }) => {
            if (
              errors.length > 0 &&
              errors[0].errorCode === "invalid_transaction"
            ) {
              setCriticalError(errors[0]);
            } else {
              setStep(ResetPasswordStep.ResetPasswordStatus);
            }
          },
        });
      }
    },
    [resetPasswordMutation]
  );

  return (
    <div className="my-10 sm:mb-10 sm:mt-0">
      <Step>
        <StepHeader>
          <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-[26px] font-semibold">
            <Icons.back
              className="absolute left-0 cursor-pointer"
              onClick={handleBack}
            />
            <div className="text-center font-semibold tracking-normal">
              {t("reset_password")}
            </div>
          </StepTitle>
          <StepDescription>{t("reset_password_description")}</StepDescription>
        </StepHeader>
        <StepDivider />
        <StepAlert className="relative">
          {/* Error message */}
          <ActionMessage hidden={!passwordResetError}>
            <p className="truncate text-xs text-destructive">
              {passwordResetError &&
                t(`error_response.${passwordResetError.errorCode}`)}
            </p>
            <Icons.close
              className="h-4 w-4 cursor-pointer text-destructive"
              onClick={() => {
                setPasswordResetError(null);
              }}
            />
          </ActionMessage>
        </StepAlert>
        <StepContent>
          <div className="flex flex-col gap-y-6 px-6 sm:px-0">
            <div className="flex flex-col gap-y-6">
              {/* New Password */}
              <FormField
                name="newPassword"
                control={control}
                render={({ field }) => (
                  <FormItem className="space-y-0">
                    <div className="space-y-2">
                      <div className="flex items-center gap-1">
                        <FormLabel>{t("new_password")}</FormLabel>
                        <LabelPopover
                          icon={
                            <Icons.info
                              className="h-4 w-4 cursor-pointer sm:h-3 sm:w-3"
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
                        </LabelPopover>
                      </div>
                      <FormControl>
                        <Input
                          {...field}
                          type="password"
                          placeholder={t("new_password_placeholder")}
                          className={cn(
                            "h-[52px] py-6",
                            passwordResetFormError.newPassword &&
                              "border-destructive"
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
              {/* Confirm New Password */}
              <FormField
                name="confirmNewPassword"
                control={control}
                render={({ field }) => (
                  <FormItem className="space-y-0">
                    <div className="space-y-2">
                      <div className="flex items-center">
                        <FormLabel>{t("confirm_new_password")}</FormLabel>
                      </div>
                      <FormControl>
                        <Input
                          {...field}
                          type="password"
                          placeholder={t("confirm_new_password_placeholder")}
                          className={cn(
                            "h-[52px] py-6",
                            passwordResetFormError.confirmNewPassword &&
                              "border-destructive"
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
            </div>
            <Button
              onClick={handleContinue}
              disabled={disabledContinue}
              isLoading={resetPasswordMutation.isPending}
            >
              {t("reset")}
            </Button>
          </div>
        </StepContent>
      </Step>
    </div>
  );
};
