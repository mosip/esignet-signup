import { useTranslation } from "react-i18next";

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

interface CancelAlertPopoverProps {
  description: string;
  handleStay: () => void;
  handleDismiss: () => void;
  keyPrefix?: string;
}

export const CancelAlertPopover = ({
  description,
  handleStay,
  handleDismiss,
  keyPrefix = "cancel_alert_popover",
}: CancelAlertPopoverProps) => {
  const { t } = useTranslation("translation", { keyPrefix });

  return (
    <AlertDialog open={true}>
      <AlertDialogContent className="rounded-[20px]">
        <AlertDialogHeader className="m-2">
          <AlertDialogTitle className="flex flex-col items-center justify-center gap-y-4 text-[1.5em]">
            {t("title")}
          </AlertDialogTitle>
          <AlertDialogDescription className="text-center text-muted-dark-gray text-md">
            {t(description, { relyingParty: "Health Service" })}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter className="sm:flex-col-reverse sm:gap-y-2 sm:space-x-0">
          <AlertDialogCancel
            id="dismiss-button"
            name="dismiss-button"
            onClick={handleDismiss}
            className={cn(
              buttonVariants({ variant: "link" }),
              "border-0 hover:bg-inherit hover:text-primary font-[500]"
            )}
          >
            {t("discontinue")}
          </AlertDialogCancel>
          <AlertDialogAction
            id="stay-button"
            name="stay-button"
            onClick={handleStay}
            className="w-full bg-primary font-[500]"
          >
            {t("stay")}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
