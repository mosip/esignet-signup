import { useCallback, useMemo } from "react";
import { yupResolver } from "@hookform/resolvers/yup";
import { Resolver, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { Outlet } from "react-router-dom";
import * as yup from "yup";

import { Form } from "~components/ui/form";
import { EkYCVerificationForm, SettingsDto } from "~typings/types";

import { EkycVerificationPopover } from "./EkycVerificationPopover";
import {
  criticalErrorSelector,
  EkycVerificationStep,
  stepSelector,
  useEkycVerificationStore,
} from "./useEkycVerificationStore";

interface EkycVerificationPageProps {
  settings: SettingsDto;
}

const ekycVerificationFormDefaultValues: EkYCVerificationForm = {
  consent: "DECLINED",
  disabilityType: null,
  verifierId: "",
};

const EKYC_VERIFICATION_VALIDATION_SCHEMA = {
  [EkycVerificationStep.VerificationSteps]: yup.object({}),
  [EkycVerificationStep.KycProviderList]: yup.object({}),
  [EkycVerificationStep.TermsAndCondition]: yup.object({}),
  [EkycVerificationStep.VideoPreview]: yup.object({}),
  [EkycVerificationStep.LoadingScreen]: yup.object({}),
  [EkycVerificationStep.VerificationScreen]: yup.object({}),
};

export const EkycVerificationPage = ({
  settings,
}: EkycVerificationPageProps) => {
  const { t } = useTranslation();

  const { step, criticalError } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        step: stepSelector(state),
        criticalError: criticalErrorSelector(state),
      }),
      []
    )
  );

  const ekycVerificationValidationSchema = useMemo(
    () => Object.values(EKYC_VERIFICATION_VALIDATION_SCHEMA),
    [settings]
  );

  const currentEkycVerificationValidationSchema =
    ekycVerificationValidationSchema[step];

  const methods = useForm<EkYCVerificationForm>({
    shouldUnregister: false,
    defaultValues: ekycVerificationFormDefaultValues,
    resolver: yupResolver(
      currentEkycVerificationValidationSchema
    ) as unknown as Resolver<EkYCVerificationForm, any>,
    mode: "onBlur",
  });

  return (
    <>
      {criticalError &&
        ["invalid_transaction", "identifier_already_registered"].includes(
          criticalError.errorCode
        ) && <EkycVerificationPopover />}

      <Form {...methods}>
        <form noValidate>
          <Outlet />
        </form>
      </Form>
    </>
  );
};
