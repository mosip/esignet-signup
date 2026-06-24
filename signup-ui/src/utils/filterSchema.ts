import { FormConfig, FormField } from "@mosip/json-form-builder/dist/types";

import { SettingsDto } from "~typings/types";

export function validateUiSpec(
  response: FormConfig | null | undefined,
  settings: SettingsDto,
  page: string
): asserts response is FormConfig {
  const isResetPasswordPage = page === "reset-pwd";

  // --- Structural validation (invalid/empty spec arrives as HTTP 200) ---
  if (!response || typeof response !== "object") {
    throw new Error(
      "Configuration error: UI spec response is missing or not a valid JSON."
    );
  }

  if (!Array.isArray(response.schema) || response.schema.length === 0) {
    throw new Error(
      "Configuration error: UI spec 'schema' is missing, empty, or not an array."
    );
  }

  // --- Configuration validation ---
  const schemaIds = new Set(response.schema.map((field: FormField) => field.id));
  const identifierKey = settings?.response?.configs?.["identifier.name"];
  const challengeFields = [
    ...new Set(response.resetPasswordChallengeFields ?? []),
  ];

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

  if (isResetPasswordPage) {
    if (!challengeFields.length) {
      throw new Error(
        "Configuration error: 'resetPasswordChallengeFields' is missing or empty. Please configure the challenge fields required for reset password."
      );
    }

    if (challengeFields.includes(identifierKey)) {
      throw new Error(
        `Configuration error: "${identifierKey}" is defined as identifier.name and is automatically used to identify the user. It must not be included in resetPasswordChallengeFields.`
      );
    }

    const missingFields = challengeFields.filter((id) => id && !schemaIds.has(id));

    if (missingFields.length) {
      throw new Error(
        `Configuration error: Some reset password challenge fields are not present in the schema: ${missingFields.join(
          ", "
        )}.`
      );
    }

    const unsupportedControlTypes = ["password", "fileupload", "photo"];
    const invalidFields = challengeFields
      .map((id) => response.schema.find((field: FormField) => field.id === id))
      .filter((field): field is FormField => {
        if (!field) {
          return false;
        }

        return unsupportedControlTypes.includes(String(field.controlType));
      });

    if (invalidFields.length) {
      throw new Error(
        `Configuration error: resetPasswordChallengeFields contains field(s) with unsupported control types:\n${invalidFields
          .map(
            (field, index) =>
              `${index + 1}) field ID '${field.id}' with control type '${field.controlType}'`
          )
          .join("\n")}`
      );
    }
  }
}

export const buildFilteredSchema = (
  response: FormConfig | null | undefined,
  settings: SettingsDto,
  page: string,
  resendOtp?: boolean
) => {
  validateUiSpec(response, settings, page);

  const isResetPasswordPage = page === "reset-pwd";

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
