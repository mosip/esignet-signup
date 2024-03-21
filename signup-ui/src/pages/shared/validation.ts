import { TFunction } from "i18next";
import * as yup from "yup";

import { SettingsDto } from "~typings/types";

export const validateUsername = (settings: SettingsDto) =>
  yup
    .string()
    .matches(/^[^0].*$/, {
      message: "username_lead_zero_validation",
      excludeEmptyString: true,
    })
    .test("isUsernameValid", "username_validation", (value) => {
      if (value === "") return true;
      return new RegExp(settings.response.configs["identifier.pattern"]).test(
        `${settings.response.configs["identifier.prefix"]}${value}`
      );
    });

export const validateCaptchaToken = () =>
  yup.string().required("captcha_token_validation");

export const validateFullName = (settings: SettingsDto) =>
  yup
    .string()
    .strict(true)
    .trim("full_name_all_spaces_validation")
    .matches(new RegExp(settings.response.configs["fullname.pattern"]), {
      message: "full_name_in_lng_validation",
      excludeEmptyString: true,
    });

export const validateOtp = (settings: SettingsDto) =>
  yup
    .string()
    .matches(new RegExp(`^\\d{${settings.response.configs["otp.length"]}}$`));

export const validatePassword = (settings: SettingsDto) =>
  yup
    .string()
    .matches(new RegExp(settings.response.configs["password.pattern"]), {
      message: "password_validation",
      excludeEmptyString: true,
    });

export const validateConfirmPassword = (
  passwordRef: string,
  settings: SettingsDto,
  isRegister: boolean
) =>
  yup
    .string()
    .matches(new RegExp(settings.response.configs["password.pattern"]), {
      message: "password_validation",
      excludeEmptyString: true,
    })
    .oneOf(
      [yup.ref(passwordRef), ""],
      isRegister
        ? "register_password_validation_must_match"
        : "password_validation_must_match"
    );
