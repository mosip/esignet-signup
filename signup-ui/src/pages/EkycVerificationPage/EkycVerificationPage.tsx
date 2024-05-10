import { useCallback } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { Outlet } from "react-router-dom";

import { Form } from "~components/ui/form";
import { SettingsDto } from "~typings/types";

import { EkycVerificationPopover } from "./EkycVerificationPopover";
import {
  criticalErrorSelector,
  stepSelector,
  useEkycVerificationStore,
} from "./useEkycVerificationStore";

interface EkycVerificationPageProps {
  settings: SettingsDto;
}

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

  const methods = useForm();

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
