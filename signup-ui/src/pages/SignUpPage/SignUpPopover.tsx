import { useCallback } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "~components/ui/alert-dialog";
import { getSignInRedirectURL } from "~utils/link";
import { useSettings } from "~pages/shared/queries";

import { criticalErrorSelector, useSignUpStore } from "./useSignUpStore";

export const SignUpPopover = () => {
  const { t } = useTranslation();

  const { data: settings } = useSettings();
  const { criticalError } = useSignUpStore(
    useCallback(
      (state) => ({
        criticalError: criticalErrorSelector(state),
      }),
      []
    )
  );
  const { hash: fromSignInHash } = useLocation();

  const handleAction = (e: any) => {
    e.preventDefault();
    window.location.href = getSignInRedirectURL(
      settings?.response.configs["signin.redirect-url"],
      fromSignInHash,
      "/signup"
    );
  };

  return (
    <AlertDialog open={!!criticalError}>
      <AlertDialogContent>
        <AlertDialogHeader className="m-2">
          <AlertDialogTitle className="flex flex-col items-center justify-center gap-y-4">
            <>
              <FailedIconSvg />
              {t("error")}
            </>
          </AlertDialogTitle>
          <AlertDialogDescription className="break-all text-center text-muted-dark-gray">
            {criticalError && t(`error_response.${criticalError.errorCode}`)}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogAction
            id="okay-button"
            name="okay-button"
            onClick={handleAction}
            className="w-full bg-primary"
          >
            {t("okay")}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
