import { useCallback, useEffect, useRef, useState } from "react";
import { JsonFormBuilder } from "@anushase/json-form-builder";
import { FormConfig } from "@anushase/json-form-builder/dist/types";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";

import {
  Step,
  StepContent,
  StepDescription,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { useRegister } from "~pages/shared/mutations";
import { useUiSpec } from "~pages/shared/queries";
import langConfigService from "~services/langConfig.service";
import { RegistrationRequestDto, SettingsDto } from "~typings/types";

import { SignUpForm } from "../SignUpPage";
import {
  setCriticalErrorSelector,
  setStepSelector,
  SignUpStep,
  useSignUpStore,
} from "../useSignUpStore";

interface AccountSetupProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountSetup = ({ settings, methods }: AccountSetupProps) => {
  const formBuilderRef: any = useRef(null); // Reference to form instance
  const { t, i18n } = useTranslation();

  const { data: uiSchemaResponse } = useUiSpec();

  const [uiSchema, setUiSchema] = useState<FormConfig | null>(null);

  const { setStep, setCriticalError } = useSignUpStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );
  const { getValues } = methods;

  const { registerMutation } = useRegister();

  const updateAfterLangChange = () => {
    const confirmPasswordField = {
      password_confirm: {
        label: {
          [i18n.language]: t("confirm_password"),
        },
        placeholder: {
          [i18n.language]: t("confirm_password_placeholder"),
        },
      },
    };
    formBuilderRef.current?.updateLanguage(
      i18n.language,
      t("login"),
      confirmPasswordField
    );
  };

  const handleSubmit = (data: any) => {
    const RegistrationRequestDto: RegistrationRequestDto = {
      requestTime: new Date().toISOString(),
      request: {
        username: `${settings.response.configs["identifier.prefix"]}${getValues(
          "phone"
        )}`,
        password: data.password,
        consent: data.consent ? "AGREE" : "DISAGREE",
        userInfo: {
          ...data,
        },
        locale: null,
      },
    };

    registerMutation.mutate(RegistrationRequestDto, {
      onSuccess: ({ errors }) => {
        if (errors && errors.length > 0) {
          if (
            errors.length > 0 &&
            errors[0].errorCode === "invalid_transaction"
          ) {
            setCriticalError(errors[0]);
          }
          updateAfterLangChange();
        } else {
          setStep(SignUpStep.AccountSetupStatus);
        }
      },
    });
  };

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
        const form = JsonFormBuilder(
          {
            schema: uiSchema.schema,
            language: {
              ...uiSchema.language,
              langCodeMap: langConfig.langCodeMapping,
            },
            allowedValues: {
              ...uiSchema.allowedValues,
              username: `${
                settings.response.configs["identifier.prefix"]
              }${getValues("phone")}`,
            },
            errors: {
              ...uiSchema.errors,
            },
          },
          "form-container",
          {
            submitButton: {
              label: t("login"),
              action: handleSubmit,
            },
            language: {
              currentLanguage: i18n.language,
              defaultLanguage: (window as any)._env_.DEFAULT_LANG,
            },
            additionalSchema: {
              password_confirm: {
                label: {
                  [i18n.language]: t("confirm_password"),
                },
                placeholder: {
                  [i18n.language]: t("confirm_password_placeholder"),
                },
              },
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
  }, [uiSchema]);

  useEffect(() => {
    setUiSchema(uiSchemaResponse?.response ?? null);
  }, [uiSchemaResponse]);

  useEffect(() => {
    updateAfterLangChange();
  }, [i18n.language]);

  return (
    <div className="my-10 sm:my-0">
      <Step>
        <StepHeader>
          <StepTitle>{t("setup_account")}</StepTitle>
          <StepDescription>{t("complete_your_registration")}</StepDescription>
        </StepHeader>
        <StepDivider />
        <StepContent className="px-10 py-8 sm:px-[18px] sm:pb-[70px] sm:pt-[22px]">
          <div id="form-container" className="registration-form"></div>
        </StepContent>
      </Step>
    </div>
  );
};
