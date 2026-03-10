import { SettingsDto } from "~typings/types";
import { FormConfig, FormField } from "@mosip/json-form-builder/dist/types";

export const buildFilteredSchema = (response: FormConfig, settings: SettingsDto, page: string, resendOtp?: boolean) => {

    const schemaIds = new Set((response?.schema || []).map((f: any) => f.id));
    const identifierKey = settings?.response?.configs?.["identifier.name"];

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
        page === "reset-pwd"
    ) {
        throw new Error(
            `Configuration error: "${identifierKey}" is defined as identifier.name and is automatically used to identify the user. It must not be included in resetPasswordChallengeFields.`
        );
    }

    if (
        (!response.resetPasswordChallengeFields ||
            !Array.isArray(response.resetPasswordChallengeFields) ||
            response.resetPasswordChallengeFields.length === 0) &&
        page === "reset-pwd"
    ) {
        throw new Error(
            "Configuration error: 'resetPasswordChallengeFields' is missing or empty. Please configure the challenge fields required for reset password."
        );
    }

    if (
        page === "reset-pwd" &&
        !response.resetPasswordChallengeFields?.every((id: string) =>
            schemaIds.has(id))
    ) {
        throw new Error(
            "Configuration error: Some reset password challenge fields are not present in the schema."
        );
    }

    const challengeFields = Array.from(
        new Set([
            settings.response.configs["identifier.name"],
            ...(page === "reset-pwd" ? response.resetPasswordChallengeFields : []),
        ])
    );

    // Filter schema
    const filteredSchema = challengeFields
        .map((id) => response.schema.find((field: FormField) => field.id === id))
        .filter((field): field is FormField => Boolean(field))
        .map((field) => ({
            ...field,
            required: page === "reset-pwd" ? true : field.required,
            disabled: resendOtp ? resendOtp : field.disabled,
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

    return {
        ...response,
        schema: filteredSchema,
        allowedValues: filteredAllowedValues,
        language: {
            ...response.language,
            mandatory:
                page === "reset-pwd"
                    ? [response.language.mandatory[0]]
                    : response.language.mandatory,
            optional: page === "reset-pwd" ? [] : response.language.optional,
        },
    };
}