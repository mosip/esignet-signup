import { useCallback } from "react";
import { useTranslation } from "react-i18next";

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
import { useSettings } from "~pages/shared/queries";

import {
  criticalErrorSelector,
  hashCodeSelector,
  useEkycVerificationStore,
} from "./useEkycVerificationStore";

export const EkycVerificationPopover = () => {
  const { t } = useTranslation();

  const { data: settings } = useSettings();
  const { criticalError, hashCode } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        criticalError: criticalErrorSelector(state),
        hashCode: hashCodeSelector(state),
      }),
      []
    )
  );

  const handleAction = (e: any) => {
    e.preventDefault();
    window.onbeforeunload = null;
    window.location.href = `${settings?.response?.configs[
      "esignet-consent.redirect-url"
    ]}?key=${hashCode?.state || ""}&error=${criticalError?.errorCode}`;
  };

  return (
    <AlertDialog open={!!criticalError}>
      <AlertDialogContent className="!w-[90vw] rounded-[20px] bg-white pb-[2rem] pt-[2.5rem]">
        <AlertDialogHeader className="m-2">
          <AlertDialogTitle className="flex flex-col items-center justify-center gap-y-4">
            <>
              <FailedIconSvg />
              {t("error")}
            </>
          </AlertDialogTitle>
          <AlertDialogDescription className="text-center text-muted-dark-gray">
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
