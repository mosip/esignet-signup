import { FormConfig, FormField } from "@mosip/json-form-builder/dist/types";

import { SettingsDto } from "~typings/types";

export const buildFilteredSchema = (
  response: FormConfig,
  settings: SettingsDto,
  page: string,
  resendOtp?: boolean
) => {
  const schemaIds = new Set((response?.schema || []).map((f: any) => f.id));
  const identifierKey = settings?.response?.configs?.["identifier.name"];
  const isResetPasswordPage = page === "reset-pwd";

  if (!identifierKey) {
    throw new Error(
      "Configuration error: 'identifier.name' is missing. Please configure the identifier field used to identify the user."
    );
  }

  if (!schemaIds.has(identifierKey)) {
    throw new Error(
      `Configuration error: Identifier field '${identifierKey}' is not present in the schema.`
    );
  }

  if (
    response.resetPasswordChallengeFields?.includes(identifierKey) &&
    isResetPasswordPage
  ) {
    throw new Error(
      `Configuration error: "${identifierKey}" is defined as identifier.name and is automatically used to identify the user. It must not be included in resetPasswordChallengeFields.`
    );
  }

  if (
    (!response.resetPasswordChallengeFields ||
      !Array.isArray(response.resetPasswordChallengeFields) ||
      response.resetPasswordChallengeFields.length === 0) &&
    isResetPasswordPage
  ) {
    throw new Error(
      "Configuration error: 'resetPasswordChallengeFields' is missing or empty. Please configure the challenge fields required for reset password."
    );
  }

  if (
    isResetPasswordPage &&
    !response.resetPasswordChallengeFields?.every((id: string) =>
      schemaIds.has(id)
    )
  ) {
    throw new Error(
      "Configuration error: Some reset password challenge fields are not present in the schema."
    );
  }

  const challengeFields = Array.from(
    new Set([
      settings.response.configs["identifier.name"],
      ...(isResetPasswordPage ? response.resetPasswordChallengeFields : []),
    ])
  );

  // Filter schema
  const filteredSchema = challengeFields
    .map((id) => response.schema.find((field: FormField) => field.id === id))
    .filter((field): field is FormField => Boolean(field))
    .map((field) => ({
      ...field,
      required: isResetPasswordPage || field.required,
      disabled: resendOtp || field.disabled,
    }));

  // Collect subTypes used
  const requiredSubTypes = new Set(
    filteredSchema.map((field: any) => field.subType).filter(Boolean)
  );

  // Filter allowedValues
  const filteredAllowedValues: Record<string, any> = {};
  const allowedValues = response.allowedValues ?? {};

  Object.keys(allowedValues).forEach((key) => {
    if (requiredSubTypes.has(key)) {
      filteredAllowedValues[key] = allowedValues[key];
    }
  });

  const mandatory = response.language?.mandatory ?? [];
  const optional = response.language?.optional ?? [];

  return {
    ...response,
    schema: filteredSchema,
    allowedValues: filteredAllowedValues,
    language: {
      ...response.language,
      mandatory: isResetPasswordPage ? mandatory.slice(0, 1) : mandatory,
      optional: isResetPasswordPage ? [] : optional,
    },
  };
};
