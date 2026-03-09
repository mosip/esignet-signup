import {
  ChangeEvent,
  ClipboardEvent,
  KeyboardEvent,
  MouseEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { JsonFormBuilder } from "@mosip/json-form-builder";
import { FormConfig, FormField } from "@mosip/json-form-builder/dist/types";
import ReCAPTCHA from "react-google-recaptcha";
import {
  ControllerRenderProps,
  FieldValues,
  useFormContext,
  UseFormReturn,
} from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { RESET_PASSWORD } from "~constants/routes";
import { ActionMessage } from "~components/ui/action-message";
// import { Button } from "~components/ui/button";
// import {
//   FormControl,
//   FormField,
//   FormItem,
//   FormLabel,
//   FormMessage,
// } from "~components/ui/form";
import { Icons } from "~components/ui/icons";
// import { Input } from "~components/ui/input";
import {
  Step,
  StepAlert,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
// import { cn } from "~utils/cn";
// import { handleInputFilter } from "~utils/input";
import { getLocale } from "~utils/language";
import { getSignInRedirectURLV2 } from "~utils/link";
import { useGenerateChallenge } from "~pages/shared/mutations";
import { useUiSpec } from "~pages/shared/queries";
import langConfigService from "~services/langConfig.service";
import {
  GenerateChallengeRequestDto,
  ResetPasswordForm,
  SettingsDto,
} from "~typings/types";
import type { Error } from "~typings/types";
import { langCodeMappingSelector, useLanguageStore } from "~/useLanguageStore";

// import { resetPasswordFormDefaultValues } from "../ResetPasswordPage";
import {
  resendOtpSelector,
  ResetPasswordStep,
  setCriticalErrorSelector,
  setResendOtpSelector,
  setStepSelector,
  setUserDataSelector,
  userDataSelector,
  useResetPasswordStore,
} from "../useResetPasswordStore";

interface UserInfoProps {
  methods: UseFormReturn<ResetPasswordForm, any, undefined>;
  settings: SettingsDto;
}

export const UserInfo = ({ settings, methods }: UserInfoProps) => {
  const formBuilderRef: any = useRef(null); // Reference to form instance
  const { i18n, t } = useTranslation();
  const navigate = useNavigate();

  const _reCaptchaRef = useRef<ReCAPTCHA>(null);
  const { hash: fromSignInHash, search } = useLocation();
  const { control, setValue, getValues } = useFormContext();
  const [challengeGenerationError, setChallengeGenerationError] =
    useState<Error | null>(null);

  const { data: uiSchemaResponse } = useUiSpec();

  const [uiSchema, setUiSchema] = useState<FormConfig | null>(null);

  const updateAfterLangChange = () => {
    formBuilderRef.current?.updateLanguage(i18n.language, t("submit"));
  };

  const { setStep, setCriticalError, resendOtp, setUserData, userData } =
    useResetPasswordStore(
      useCallback(
        (state) => ({
          setStep: setStepSelector(state),
          setCriticalError: setCriticalErrorSelector(state),
          resendOtp: resendOtpSelector(state),
          setResendOtp: setResendOtpSelector(state),
          setUserData: setUserDataSelector(state),
          userData: userDataSelector(state),
        }),
        []
      )
    );

  const { generateChallengeMutation } = useGenerateChallenge();

  function buildResetPasswordSchema(response: FormConfig) {
    const schemaIds = new Set((response?.schema || []).map((f) => f.id));
    const identifierKey = settings?.response?.configs?.["identifier.name"];

    try {
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

      if (response.resetPasswordChallengeFields?.includes(identifierKey)) {
        throw new Error(
          `Configuration error: "${identifierKey}" is defined as identifier.name and is automatically used to identify the user. It must not be included in resetPasswordChallengeFields.`
        );
      }

      if (
        !response.resetPasswordChallengeFields ||
        !Array.isArray(response.resetPasswordChallengeFields) ||
        response.resetPasswordChallengeFields.length === 0
      ) {
        throw new Error(
          "Configuration error: 'resetPasswordChallengeFields' is missing or empty. Please configure the challenge fields required for reset password."
        );
      }

      if (
        !response.resetPasswordChallengeFields.every((id) => schemaIds.has(id))
      ) {
        throw new Error(
          "Configuration error: Some reset password challenge fields are not present in the schema."
        );
      }
    } catch (err) {
      console.error(err);
      navigate("/something-went-wrong");
    }

    const challengeFields = Array.from(
      new Set([
        settings.response.configs["identifier.name"],
        ...response.resetPasswordChallengeFields,
      ])
    );

    // Filter schema
    const filteredSchema = challengeFields
      .map((id) => response.schema.find((field: FormField) => field.id === id))
      .filter((field): field is FormField => Boolean(field))
      .map((field) => ({
        ...field,
        required: true,
        disabled: resendOtp,
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
        mandatory: [response.language.mandatory[0]],
        optional: [],
      },
    };
  }

  useEffect(() => {
    return () => {
      (window as any).__form_rendered__ = false;
      formBuilderRef.current = null;
      const container = document.getElementById("form-container");
      if (container) container.innerHTML = ""; // optional: clean old content
    };
  }, []);

  useEffect(() => {
    if (!uiSchema) return;
    langConfigService.getLocaleConfiguration().then((langConfig) => {
      if (JsonFormBuilder && !(window as any).__form_rendered__) {
        let restUserData;

        if (userData) {
          const { recaptchaToken, ...rest } = userData;
          restUserData = rest;
        }

        const form = JsonFormBuilder(
          {
            ...uiSchema,
            prefilledValues: {
              ...(resendOtp ? restUserData : userData),
            },
            language: {
              ...uiSchema.language,
              langCodeMap: langConfig.langCodeMapping,
            },
          },
          "form-container",
          {
            submitButton: {
              label: t("submit"),
              action: handleContinue as any,
            },
            language: {
              currentLanguage: i18n.language,
              defaultLanguage: (window as any)._env_.DEFAULT_LANG,
            },
            recaptcha: {
              siteKey: settings.response.configs["captcha.site.key"],
              enabled:
                settings.response.configs["send-challenge.captcha.required"],
              language: i18n.language,
            },
          }
        );
        form.render();
        formBuilderRef.current = form; // Store the form instance in the ref
        (window as any).__form_rendered__ = true; // Indicate that the form has been rendered
      } else if (!JsonFormBuilder) {
        console.error("JsonFormBuilder is not defined.");
      }
    });
  }, [uiSchema, resendOtp]);

  useEffect(() => {
    if (uiSchemaResponse && uiSchemaResponse.response) {
      setUiSchema(buildResetPasswordSchema(uiSchemaResponse?.response) ?? null);
    }
  }, [uiSchemaResponse]);

  useEffect(() => {
    updateAfterLangChange();
  }, [i18n.language]);

  const handleContinue = useCallback(
    async (data: any) => {
      if (generateChallengeMutation.isPending) return;

      const identifierKey = settings.response.configs["identifier.name"];

      const generateChallengeRequestDto: GenerateChallengeRequestDto = {
        requestTime: new Date().toISOString(),
        request: {
          identifier: data[identifierKey],
          captchaToken: data.recaptchaToken,
          locale: i18n.language,
          regenerateChallenge: resendOtp ? true : false,
          purpose: "RESET_PASSWORD",
        },
      };

      setUserData(data);

      return generateChallengeMutation.mutate(generateChallengeRequestDto, {
        onSuccess: ({ response, errors }) => {
          if (errors.length > 0) {
            if (errors[0].errorCode === "invalid_transaction") {
              setCriticalError(errors[0]);
            } else {
              setChallengeGenerationError(errors[0]);
            }
            _reCaptchaRef.current?.reset();
            setValue("captchaToken", "", { shouldValidate: true });
          }

          if (response && errors.length === 0) {
            setStep(ResetPasswordStep.Otp);
          }
        },
      });
    },
    [generateChallengeMutation]
  );

  return (
    <div className="my-10 sm:mb-10 sm:mt-0">
      <Step>
        <StepHeader>
          <StepTitle className="relative flex w-full items-center justify-center gap-x-4 font-semibold">
            {!!fromSignInHash && (
              <a
                href={getSignInRedirectURLV2(
                  settings?.response.configs["signin.redirect-url"],
                  fromSignInHash,
                  search,
                  RESET_PASSWORD
                )}
                className="absolute left-0 cursor-pointer"
              >
                <Icons.back id="back-button" name="back-button" />
              </a>
            )}
            {resendOtp ? (
              <div className="grow px-3 text-center text-[16px] font-semibold tracking-normal xs:px-2">
                {t("captcha_required")}
              </div>
            ) : (
              <div className="text-center text-[26px] font-semibold tracking-normal">
                {t("forgot_password")}
              </div>
            )}
          </StepTitle>
          {!resendOtp && (
            <StepDescription>
              <div className="mt-2">{t("forgot_password_description")}</div>
            </StepDescription>
          )}
        </StepHeader>
        <StepDivider />
        <StepAlert className="relative">
          {/* Error message */}
          <ActionMessage hidden={!challengeGenerationError}>
            <p className="text-xs text-destructive">
              {challengeGenerationError &&
                t(`error_response.${challengeGenerationError.errorCode}`)}
            </p>
            <Icons.close
              className="h-4 w-4 cursor-pointer text-destructive"
              onClick={() => {
                setChallengeGenerationError(null);
              }}
            />
          </ActionMessage>
        </StepAlert>
        <StepContent>
          {/* Phone and reCAPTCHA inputs */}
          <div id="form-container" className="registration-form"></div>
        </StepContent>
      </Step>
    </div>
  );
};
