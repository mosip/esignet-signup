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
import { useRegister, useUploadFile } from "~pages/shared/mutations";
import { useUiSpec } from "~pages/shared/queries";
import langConfigService from "~services/langConfig.service";
import {
  RegisterResponseDto,
  RegistrationRequestDto,
  SettingsDto,
  UploadFilePossibleErrors,
} from "~typings/types";

import { SignUpForm } from "../SignUpPage";
import {
  criticalErrorSelector,
  setCriticalErrorSelector,
  setStepSelector,
  SignUpStep,
  useSignUpStore,
} from "../useSignUpStore";
import { UploadFileErrorModal } from "./components/UploadFileErrorModal";

interface AccountSetupProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountSetup = ({ settings, methods }: AccountSetupProps) => {
  const formBuilderRef: any = useRef(null); // Reference to form instance
  const { t, i18n } = useTranslation();

  const fileUploadTypeList = ["photo"];

  const identifierName =
    settings?.response.configs["identifier.name"] || "username";

  const { data: uiSchemaResponse } = useUiSpec();

  const [uiSchema, setUiSchema] = useState<FormConfig | null>(null);

  const { setStep, criticalError, setCriticalError } = useSignUpStore(
    useCallback(
      (state) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
        criticalError: criticalErrorSelector(state),
      }),
      []
    )
  );
  const { getValues } = methods;

  const { registerMutation } = useRegister();
  const { uploadFileMutation } = useUploadFile({
    retryAttempt: settings.response.configs["upload.request.limit"] || 3,
    retryDelay: settings.response.configs["upload.request.delay"] || 10,
  });

  const updateAfterLangChange = () => {
    formBuilderRef.current?.updateLanguage(i18n.language, t("submit"));
  };

  const uploadFile = async (data: any) => {
    // converting ui_spec to upload mutation calls
    // finding all file upload fields with value
    const uploadMutationList = uiSchema?.schema
      .filter(
        ({ id, controlType }) =>
          fileUploadTypeList.includes(controlType) && data[id].value
      )
      .map(({ id }) => {
        const temp = new FormData();
        temp.append("file", data[id].value);
        temp.append("field", id);
        delete data[id]; // remove file field from form data to avoid sending it in registration request
        return uploadFileMutation.mutateAsync(temp);
      });

    const uploadResults = await Promise.all(uploadMutationList || []);
    const allSuccess = uploadResults.every((res: RegisterResponseDto) => {
      if (res.response?.status === "UPLOADED") {
        return true;
      }
      return false;
    });
    if (!allSuccess) {
      throw new Error("upload_failed");
    }
  };

  const handleSubmit = async (data: any) => {
    // try to upload the files first
    // if upload fails, go to the last step to show error
    try {
      await uploadFile(data);
    } catch (error) {
      setCriticalError({
        errorCode: "upload_failed",
        errorMessage: "File upload failed",
      });
      return;
    }

    const RegistrationRequestDto: RegistrationRequestDto = {
      requestTime: new Date().toISOString(),
      request: {
        username: data[identifierName] || data.username,
        password: data.password,
        consent: data.consent ? "AGREE" : "DISAGREE",
        userInfo: {
          ...data,
        },
        locale: i18n.language,
      },
    };

    registerMutation.mutate(RegistrationRequestDto, {
      onSuccess: ({ errors }) => {
        if (errors && errors.length > 0) {
          if (errors.length > 0) {
            setCriticalError(errors[0]);
            setStep(SignUpStep.AccountRegistrationStatus);
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
            ...uiSchema,
            language: {
              ...uiSchema.language,
              langCodeMap: langConfig.langCodeMapping,
            },
            allowedValues: {
              ...uiSchema.allowedValues,
              [identifierName]: `${
                settings.response.configs["identifier.prefix"]
              }${getValues("phone")}`,
            },
          },
          "form-container",
          {
            submitButton: {
              label: t("submit"),
              action: handleSubmit,
            },
            language: {
              currentLanguage: i18n.language,
              defaultLanguage: (window as any)._env_.DEFAULT_LANG,
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

  // Show upload error modal if upload fails
  if (
    criticalError &&
    [...new Set(UploadFilePossibleErrors)].includes(criticalError.errorCode)
  ) {
    return <UploadFileErrorModal />;
  }

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
