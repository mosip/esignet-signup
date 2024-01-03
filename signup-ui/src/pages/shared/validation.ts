import { TFunction } from "i18next";
import * as yup from "yup";

import { SettingsDto } from "~typings/types";

export const validateUsername = (settings: SettingsDto, t: TFunction) =>
  yup
    .string()
    .required(t("username_validation"))
    .matches(/^[^0].*$/, t("username_validation"))
    .test("isUsernameValid", t("username_validation"), (value) =>
      new RegExp(settings.response.configs["identifier.pattern"]).test(
        `${settings.response.configs["identifier.prefix"]}${value}`
      )
    );

export const validateCaptchaToken = (t: TFunction) =>
  yup.string().required(t("captcha_token_validation"));

export const validateFullName = (settings: SettingsDto, t: TFunction) =>
  yup
    .string()
    .required(t("full_name_validation"))
    .matches(
      new RegExp(settings.response.configs["fullname.pattern"]),
      t("full_name_in_lng_validation")
    );

export const validateOtp = (settings: SettingsDto) =>
  yup
    .string()
    .matches(new RegExp(`^\\d{${settings.response.configs["otp.length"]}}$`));

export const validatePassword = (settings: SettingsDto, t: TFunction) =>
  yup
    .string()
    .required(t("password_validation"))
    .matches(
      new RegExp(settings.response.configs["password.pattern"]),
      t("password_validation")
    );

export const validateConfirmPassword = (
  passwordRef: string,
  settings: SettingsDto,
  t: TFunction
) =>
  yup
    .string()
    .matches(
      new RegExp(settings.response.configs["password.pattern"]),
      t("password_validation")
    )
    .oneOf([yup.ref(passwordRef)], t("password_validation_must_match"));
