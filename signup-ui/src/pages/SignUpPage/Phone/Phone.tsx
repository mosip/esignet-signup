import { useCallback, useEffect, useRef, useState } from "react";
import { JsonFormBuilder } from "@mosip/json-form-builder";
import { FormConfig } from "@mosip/json-form-builder/dist/types";
import ReCAPTCHA from "react-google-recaptcha";
import { useFormContext, UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { ActionMessage } from "~components/ui/action-message";
import { Icons } from "~components/ui/icons";
import {
  Step,
  StepAlert,
  StepContent,
  StepDivider,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { buildFilteredSchema } from "~utils/filterSchema";
import { getSignInRedirectURLV2 } from "~utils/link";
import { useGenerateChallenge } from "~pages/shared/mutations";
import { useUiSpec } from "~pages/shared/queries";
import langConfigService from "~services/langConfig.service";
import {
  Error,
  GenerateChallengeRequestDto,
  SettingsDto,
} from "~typings/types";

import { SignUpForm } from "../SignUpPage";
import {
  resendOtpSelector,
  setCriticalErrorSelector,
  setResendOtpSelector,
  setStepSelector,
  setUserDataSelector,
  SignUpStep,
  userDataSelector,
  useSignUpStore,
} from "../useSignUpStore";

interface PhoneProps {
  settings: SettingsDto;
  methods: UseFormReturn<SignUpForm, any, undefined>;
}
export const Phone = ({ settings, methods }: PhoneProps) => {
  const formBuilderRef: any = useRef(null); // Reference to form instance
  const { i18n, t } = useTranslation();

  const { data: uiSchemaResponse } = useUiSpec();
  const navigate = useNavigate();
  const [uiSchema, setUiSchema] = useState<FormConfig | null>(null);

  const updateAfterLangChange = () => {
    formBuilderRef.current?.updateLanguage(i18n.language, t("submit"));
  };

  const {
    setStep,
    setCriticalError,
    resendOtp,
    setResendOtp,
    setUserData,
    userData,
  } = useSignUpStore(
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

  const [hasError, setHasError] = useState<boolean>(false);
  const { control, setValue, getValues } = useFormContext();
  const { generateChallengeMutation } = useGenerateChallenge();
  const _reCaptchaRef = useRef<ReCAPTCHA>(null);
  const [error, setError] = useState<Error | null>(null);
  const { hash: fromSignInHash, search } = useLocation();

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
    if (!uiSchemaResponse?.response) return;

    try {
      const schema = buildFilteredSchema(
        uiSchemaResponse.response,
        settings,
        "signup",
        resendOtp
      );

      setUiSchema(schema ?? null);
    } catch (err) {
      console.error(err);
      navigate("/something-went-wrong");
    }
  }, [uiSchemaResponse]);

  useEffect(() => {
    updateAfterLangChange();
  }, [i18n.language]);

  useEffect(() => {
    if (!hasError) return;

    const intervalId = setInterval(() => {
      setHasError(false);
    }, settings.response.configs["popup.timeout"] * 1000);

    return () => clearInterval(intervalId);
  }, [hasError]);

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
          purpose: "REGISTRATION",
        },
      };

      setUserData(data);

      return generateChallengeMutation.mutate(generateChallengeRequestDto, {
        onSuccess: ({ response, errors }) => {
          if (errors.length > 0) {
            if (errors[0].errorCode === "invalid_transaction") {
              setCriticalError(errors[0]);
            } else {
              setError(errors[0]);
              setHasError(true);
            }
            _reCaptchaRef.current?.reset();
            setValue("captchaToken", "", { shouldValidate: true });
          }

          if (response && errors.length === 0) {
            setStep(SignUpStep.Otp);
            setResendOtp(false);
          }
        },
        onError: () => {
          setHasError(true);
        },
      });
    },
    [generateChallengeMutation, getValues, setStep, setValue]
  );

  return (
    <Step>
      <StepHeader className="px-6">
        <StepTitle className="flex w-full items-center justify-center text-base font-semibold leading-5">
          {!!fromSignInHash && (
            <a
              href={getSignInRedirectURLV2(
                settings?.response.configs["signin.redirect-url"],
                fromSignInHash,
                search,
                "/signup"
              )}
              className="flex-none cursor-pointer"
            >
              <Icons.back id="back-button" name="back-button" />
            </a>
          )}
          {resendOtp ? (
            <div className="grow px-3 text-center font-semibold tracking-normal xs:px-2">
              {t("captcha_required")}
            </div>
          ) : (
            <div className="grow px-3 text-center font-semibold tracking-normal xs:px-2">
              {t("enter_your_number", {
                identifier: t(
                  settings.response.configs["identifier.name"] as any
                ),
              })}
            </div>
          )}
        </StepTitle>
      </StepHeader>
      <StepDivider />
      <StepAlert className="relative">
        {/* Error message */}
        <ActionMessage hidden={!hasError}>
          <p className="text-xs text-destructive">
            {error && t(`error_response.${error.errorCode}`)}
          </p>
          <Icons.close
            className="h-4 w-4 cursor-pointer text-destructive"
            onClick={() => {
              setHasError(false);
            }}
          />
        </ActionMessage>
      </StepAlert>
      <StepContent>
        <div id="form-container" className="registration-form"></div>
      </StepContent>
    </Step>
  );
};
