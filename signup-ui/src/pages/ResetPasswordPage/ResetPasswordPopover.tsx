import { useCallback } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useLocation } from "react-router-dom";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import { RESET_PASSWORD } from "~constants/routes";
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

import {
  criticalErrorSelector,
  useResetPasswordStore,
} from "./useResetPasswordStore";
import { ResetPasswordPossibleInvalid } from "~typings/types";

export const ResetPasswordPopover = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { data: settings } = useSettings();

  const { criticalError } = useResetPasswordStore(
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
    if (ResetPasswordPossibleInvalid.includes(criticalError?.errorCode!!)) {
      document.location.reload();
    } else {
      window.location.href = getSignInRedirectURL(
        settings?.response.configs["signin.redirect-url"],
        fromSignInHash,
        RESET_PASSWORD
      );
    }

  };

  return (
    <AlertDialog open={!!criticalError}>
      <AlertDialogContent>
        <AlertDialogHeader className="m-2">
          <AlertDialogTitle className="flex flex-col items-center justify-center gap-y-4">
            <>
              <FailedIconSvg />
              {ResetPasswordPossibleInvalid.includes(criticalError?.errorCode!!) ? t("invalid") : t("error")}
            </>
          </AlertDialogTitle>
          <AlertDialogDescription className="text-balance text-center text-muted-dark-gray">
            {criticalError && t(`error_response.${criticalError.errorCode}`)}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogAction
            onClick={handleAction}
            className="w-full bg-primary"
          >
            {ResetPasswordPossibleInvalid.includes(criticalError?.errorCode!!) ? t("retry") : t("okay")}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
