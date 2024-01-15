import { useCallback, useEffect, useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";

import { Form } from "~components/ui/form";
import {
  validateCaptchaToken,
  validateConfirmPassword,
  validateFullName,
  validateOtp,
  validatePassword,
  validateUsername,
} from "~pages/shared/validation";
import { ResetPasswordForm, SettingsDto } from "~typings/types";

import Otp from "./Otp";
import ResetPassword from "./ResetPassword";
import ResetPasswordConfirmation from "./ResetPasswordConfirmation";
import { ResetPasswordPopover } from "./ResetPasswordPopover";
import ResetPasswordStatus from "./ResetPasswordStatus";
import {
  criticalErrorSelector,
  ResetPasswordStep,
  stepSelector,
  useResetPasswordStore,
} from "./useResetPasswordStore";
import UserInfo from "./UserInfo";

export const resetPasswordFormDefaultValues: ResetPasswordForm = {
  username: "",
  fullname: "",
  captchaToken: "",
  otp: "",
  newPassword: "",
  confirmNewPassword: "",
};

interface ResetPasswordPageProps {
  settings: SettingsDto;
}

export const ResetPasswordPage = ({ settings }: ResetPasswordPageProps) => {
  const { t } = useTranslation();

  const { step, criticalError } = useResetPasswordStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        criticalError: criticalErrorSelector(state),
      }),
      []
    )
  );

  const validationSchema = useMemo(
    () => [
      // Step 1 - UserInfo
      yup.object({
        username: validateUsername(settings, t),
        fullname: validateFullName(settings, t),
        captchaToken: validateCaptchaToken(t),
      }),
      // Step 2 - Otp
      yup.object({
        otp: validateOtp(settings),
      }),
      // Step 3 - ResetPassword
      yup.object({
        newPassword: validatePassword(settings, t),
        confirmNewPassword: validateConfirmPassword("newPassword", settings, t, false),
      }),
      // Step 4 - ResetPasswordStatus
      yup.object({}),
      // Step 5 - ResetPasswordConfirmation
      yup.object({}),
    ],
    [settings, t]
  );

  const currentValidationSchema = validationSchema[step];

  const methods = useForm<ResetPasswordForm>({
    shouldUnregister: false,
    defaultValues: resetPasswordFormDefaultValues,
    resolver: yupResolver(currentValidationSchema) as unknown as Resolver<
      ResetPasswordForm,
      any
    >,
    mode: "onBlur",
  });

  const {
    formState: { isDirty },
  } = methods;

  useEffect(() => {
    if (
      step === ResetPasswordStep.ResetPasswordConfirmation ||
      (criticalError && criticalError.errorCode === "invalid_transaction")
    )
      return;

    const handleTabBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();

      return (
        isDirty && (event.returnValue = t("reset_password_discontinue_prompt"))
      );
    };

    window.addEventListener("beforeunload", handleTabBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleTabBeforeUnload);
    };
  }, [step, criticalError]);

  const getResetPasswordContent = (step: ResetPasswordStep) => {
    switch (step) {
      case ResetPasswordStep.UserInfo:
        return <UserInfo methods={methods} settings={settings} />;
      case ResetPasswordStep.Otp:
        return <Otp methods={methods} settings={settings} />;
      case ResetPasswordStep.ResetPassword:
        return <ResetPassword methods={methods} settings={settings} />;
      case ResetPasswordStep.ResetPasswordStatus:
        return <ResetPasswordStatus settings={settings} />;
      case ResetPasswordStep.ResetPasswordConfirmation:
        return <ResetPasswordConfirmation />;
      default:
        return "unknown step";
    }
  };

  return (
    <>
      {criticalError &&
        ["invalid_transaction", "knowledgebase_mismatch"].includes(criticalError.errorCode) && (
          <ResetPasswordPopover />
        )}
      <Form {...methods}>
        <form>{getResetPasswordContent(step)}</form>
      </Form>
    </>
  );
};
