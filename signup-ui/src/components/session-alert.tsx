import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useIdleTimer } from "react-idle-timer";
import { useLocation } from "react-router-dom";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "~components/ui/alert-dialog";
import { Icons } from "~components/ui/icons";
import { getSignInRedirectURL } from "~utils/link";
import { convertTime } from "~utils/timer";
import { useSettings } from "~pages/shared/queries";

// TODO: replace with the timeout and promptBeforeIdle from API
const timeout = 15_000;
const promptBeforeIdle = 12_000;
const ON_ACTION_THROTTLE = 500;

interface SessionAlertProps {
  isInSessionTimeoutScope: boolean;
}

export const SessionAlert = ({
  isInSessionTimeoutScope,
}: SessionAlertProps) => {
  const { t } = useTranslation();

  const { data: settings } = useSettings();
  const { hash: fromSignInHash } = useLocation();

  const [sessionRemainingTimeout, setSessionRemainingTimeout] =
    useState<number>(timeout);
  const [openSessionAlert, setOpenSessionAlert] = useState<boolean>(false);

  const handleOnActive = useCallback(() => {
    setOpenSessionAlert(false);
  }, []);

  const handleOnPrompt = useCallback(() => {
    setOpenSessionAlert(true);
  }, []);

  const handleOnIdle = useCallback(() => {
    setOpenSessionAlert(true);
  }, []);

  const { getRemainingTime, activate, isIdle } = useIdleTimer({
    onActive: handleOnActive,
    onIdle: handleOnIdle,
    onPrompt: handleOnPrompt,
    timeout,
    promptBeforeIdle,
    throttle: ON_ACTION_THROTTLE,
    stopOnIdle: true,
  });

  useEffect(() => {
    const interval = setInterval(() => {
      setSessionRemainingTimeout(Math.ceil(getRemainingTime() / 1000));
    }, ON_ACTION_THROTTLE);

    return () => {
      clearInterval(interval);
    };
  });

  const handleContinueSession = useCallback(() => {
    activate();
    handleOnActive();
  }, []);

  const handleReturnToLogin = (e: any) => {
    e.preventDefault();
    window.onbeforeunload = null;
    window.location.href = getSignInRedirectURL(
      settings?.response.configs["signin.redirect-url"],
      fromSignInHash,
      "/signup"
    );
  };

  return (
    <SessionAlertDialog
      showSessionAlert={openSessionAlert}
      isInSessionTimeoutScope={isInSessionTimeoutScope}
      isIdle={isIdle}
      sessionRemainingTimeout={sessionRemainingTimeout}
      handleReturnToLogin={handleReturnToLogin}
      handleContinueSession={handleContinueSession}
    />
  );
};

interface SessionAlertDialogProps {
  showSessionAlert: boolean;
  isInSessionTimeoutScope: boolean;
  isIdle: () => boolean;
  sessionRemainingTimeout: number;
  handleReturnToLogin: (e: any) => void;
  handleContinueSession: () => void;
}

export const SessionAlertDialog = ({
  showSessionAlert,
  isInSessionTimeoutScope,
  isIdle,
  sessionRemainingTimeout,
  handleReturnToLogin,
  handleContinueSession,
}: SessionAlertDialogProps) => {
  const { t } = useTranslation();

  return (
    <AlertDialog open={showSessionAlert && isInSessionTimeoutScope}>
      <AlertDialogContent
        className="rounded-2xl ring-0"
        data-testid="session-alert-dialog"
      >
        <AlertDialogHeader className="m-2">
          <AlertDialogTitle className="flex flex-col items-center justify-center gap-y-4 text-2xl">
            <>
              <Icons.failed />
              {isIdle() ? t("session.expired.title") : t("session.alert.title")}
            </>
          </AlertDialogTitle>
          <div className="text-center text-muted-dark-gray">
            {isIdle() ? (
              t("session.expired.description")
            ) : (
              <>
                <p>{t("session.alert.description")}</p>
                <p className="mt-2 inline-block rounded-md bg-alert p-2 px-8 text-center text-sm font-semibold text-primary">
                  {t("session.alert.countDown", {
                    remainingTimeBeforePrompt: convertTime(
                      sessionRemainingTimeout
                    ),
                  })}
                </p>
              </>
            )}
          </div>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogAction
            id="cs-button"
            name="cs-button"
            onClick={isIdle() ? handleReturnToLogin : handleContinueSession}
            className="w-full bg-primary"
          >
            {isIdle()
              ? t("session.expired.returnToLogin")
              : t("session.alert.continueSession")}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
