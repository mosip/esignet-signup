import { yupResolver } from "@hookform/resolvers/yup";
import { isEqual } from "lodash";
import { useCallback, useEffect, useMemo } from "react";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";

import { criticalErrorsToPopup } from "~constants/criticalErrors";
import { Form } from "~components/ui/form";
import {
  validateCaptchaToken,
  validateConfirmPassword,
  validateFullName,
  validateOtp,
  validatePassword,
  validateUsername,
} from "~pages/shared/validation";
import {
  ResetPasswordForm,
  ResetPasswordPossibleInvalid,
  SettingsDto,
} from "~typings/types";

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
        username: validateUsername(settings),
        fullname: validateFullName(settings, t),
        captchaToken: validateCaptchaToken(settings),
      }),
      // Step 2 - Otp
      yup.object({
        otp: validateOtp(settings),
      }),
      // Step 3 - ResetPassword
      yup.object({
        newPassword: validatePassword(settings),
        confirmNewPassword: validateConfirmPassword(
          "newPassword",
          settings,
          false
        ),
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
    getValues,
    formState: { isDirty },
  } = methods;

  useEffect(() => {
    if (isEqual(resetPasswordFormDefaultValues, getValues())) return;

    if (
      step === ResetPasswordStep.ResetPasswordConfirmation ||
      (criticalError &&
        ["invalid_transaction", ...ResetPasswordPossibleInvalid].includes(
          criticalError.errorCode
        ))
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
  }, [step, criticalError, getValues()]);

  // useEffect(() => {
  //   methods.trigger();
  // }, [i18n.language, methods]);

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
        [
          ...new Set([
            "invalid_transaction",
            ...ResetPasswordPossibleInvalid,
            ...criticalErrorsToPopup
          ])
        ].includes(criticalError.errorCode) && <ResetPasswordPopover />}
      <Form {...methods}>
        <form noValidate>{getResetPasswordContent(step)}</form>
      </Form>
    </>
  );
};
