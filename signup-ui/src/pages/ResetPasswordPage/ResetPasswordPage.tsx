import { useCallback, useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";

import { Form } from "~components/ui/form";
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
        username: yup
          .string()
          .required(t("username_validation"))
          .matches(/^[^0].*$/, t("username_validation"))
          .matches(
            new RegExp(settings.response.configs["identifier.pattern"]),
            t("username_validation")
          ),
        fullname: yup
          .string()
          .required(t("full_name_validation"))
          .matches(
            new RegExp(settings.response.configs["fullname.pattern"]),
            t("full_name_in_lng_validation")
          ),
        captchaToken: yup.string().required(t("captcha_token_validation")),
      }),
      // Step 2 - Otp
      yup.object({
        otp: yup
          .string()
          .matches(
            new RegExp(`^\\d{${settings.response.configs["otp.length"]}}$`)
          ),
      }),
      // Step 3 - ResetPassword
      yup.object({
        newPassword: yup
          .string()
          .required(t("password_validation"))
          .matches(
            new RegExp(settings.response.configs["password.pattern"]),
            t("password_validation")
          ),
        confirmNewPassword: yup
          .string()
          .matches(
            new RegExp(settings.response.configs["password.pattern"]),
            t("password_validation")
          )
          .oneOf([yup.ref("password")], t("password_validation_must_match")),
      }),
      // Step 4 - ResetPasswordStatus
      yup.object({}),
      // Step 5 - ResetPasswordConfirmation
      yup.object({}),
    ],
    [settings, t]
  );

  const currentValidationSchema = validationSchema[step];

  const resetPasswordFormDefaultValues: ResetPasswordForm = {
    username: "",
    fullname: "",
    captchaToken: "",
    otp: "",
    newPassword: "",
    confirmNewPassword: "",
  };

  const methods = useForm<ResetPasswordForm>({
    shouldUnregister: false,
    defaultValues: resetPasswordFormDefaultValues,
    resolver: yupResolver(currentValidationSchema) as unknown as Resolver<
      ResetPasswordForm,
      any
    >,
    mode: "onBlur",
  });

  const getResetPasswordContent = (step: ResetPasswordStep) => {
    switch (step) {
      case ResetPasswordStep.UserInfo:
        return <UserInfo methods={methods} settings={settings} />;
      case ResetPasswordStep.Otp:
        return <Otp />;
      case ResetPasswordStep.ResetPassword:
        return <ResetPassword />;
      case ResetPasswordStep.ResetPasswordStatus:
        return <ResetPasswordStatus />;
      case ResetPasswordStep.ResetPasswordConfirmation:
        return <ResetPasswordConfirmation />;
      default:
        return "unknown step";
    }
  };

  return (
    <>
      {criticalError &&
        ["invalid_transaction"].includes(criticalError.errorCode) && (
          <ResetPasswordPopover />
        )}
      <Form {...methods}>
        <form>{getResetPasswordContent(step)}</form>
      </Form>
    </>
  );
};
