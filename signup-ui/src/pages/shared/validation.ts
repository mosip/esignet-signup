import { TFunction } from "i18next";
import * as yup from "yup";

import { SettingsDto } from "~typings/types";

export const validateUsername = (settings: SettingsDto, t: TFunction) =>
  yup
    .string()
    .trim()
    .matches(/^[^0].*$/, {
      message: t("username_lead_zero_validation"),
      excludeEmptyString: true,
    })
    .test("isUsernameValid", t("username_validation"), (value) => {
      if (value === "") return true;
      return new RegExp(settings.response.configs["identifier.pattern"]).test(
        `${settings.response.configs["identifier.prefix"]}${value}`
      );
    });

export const validateCaptchaToken = (t: TFunction) =>
  yup.string().required(t("captcha_token_validation"));

export const validateFullName = (settings: SettingsDto, t: TFunction) =>
  yup
    .string()
    .strict(true)
    .trim(t("full_name_in_lng_validation"))
    .matches(new RegExp(settings.response.configs["fullname.pattern"]), {
      message: t("full_name_in_lng_validation"),
      excludeEmptyString: true,
    });

export const validateOtp = (settings: SettingsDto) =>
  yup
    .string()
    .matches(new RegExp(`^\\d{${settings.response.configs["otp.length"]}}$`));

export const validatePassword = (settings: SettingsDto, t: TFunction) =>
  yup
    .string()
    .trim()
    .matches(new RegExp(settings.response.configs["password.pattern"]), {
      message: t("password_validation"),
      excludeEmptyString: true,
    });

export const validateConfirmPassword = (
  passwordRef: string,
  settings: SettingsDto,
  t: TFunction,
  isRegister: boolean
) =>
  yup
    .string()
    .trim()
    .matches(new RegExp(settings.response.configs["password.pattern"]), {
      message: t("password_validation"),
      excludeEmptyString: true,
    })
    .oneOf([yup.ref(passwordRef), ""], isRegister ? t("register_password_validation_must_match") : t("password_validation_must_match"));
