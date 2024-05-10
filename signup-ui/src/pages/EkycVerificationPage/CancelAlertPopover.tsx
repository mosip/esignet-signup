import { useCallback } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "~components/ui/alert-dialog";
import { buttonVariants } from "~components/ui/button";
import { cn } from "~utils/cn";
import { getSignInRedirectURL } from "~utils/link";

interface CancelAlertPopoverProps {
  description: string;
  handleStay: () => void;
  handleDismiss: () => void;
}

export const CancelAlertPopover = ({
  description,
  handleStay,
  handleDismiss,
}: CancelAlertPopoverProps) => {
  const { t } = useTranslation("translation", {
    keyPrefix: "cancel_alert_popover",
  });

  return (
    <AlertDialog open={true}>
      <AlertDialogContent>
        <AlertDialogHeader className="m-2">
          <AlertDialogTitle className="flex flex-col items-center justify-center gap-y-4">
            {t("title")}
          </AlertDialogTitle>
          <AlertDialogDescription className="break-all text-center text-muted-dark-gray">
            {t(description, { relyingParty: "Health Service" })}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel
            id="dismiss-button"
            name="dismiss-button"
            onClick={handleDismiss}
            className={cn(
              buttonVariants({ variant: "link" }),
              "border-0 hover:bg-inherit hover:text-primary"
            )}
          >
            {t("discontinue")}
          </AlertDialogCancel>
          <AlertDialogAction
            id="stay-button"
            name="stay-button"
            onClick={handleStay}
            className="w-full bg-primary"
          >
            {t("stay")}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
