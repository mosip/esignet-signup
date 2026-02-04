import { MouseEventHandler, useEffect, useMemo } from "react";
import { Trans, useTranslation } from "react-i18next";
import { useTimer } from "react-timer-hook";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { convertTime, getTimeoutTime } from "~utils/timer";

interface IdentityVerificationStatusLayoutProps {
  status: "success" | "failed";
  title: string;
  description: string;
  btnLabel?: string;
  onBtnClick?: MouseEventHandler<HTMLButtonElement> | undefined;
  autoRedirect?: boolean;
  autoRedirectDelay?: number;
}

export const IdentityVerificationStatusLayout = ({
  status,
  title,
  description,
  btnLabel,
  onBtnClick,
  autoRedirect = false,
  autoRedirectDelay = 10,
}: IdentityVerificationStatusLayoutProps) => {
  const { t } = useTranslation();

  const { totalSeconds, restart, pause } = useTimer({
    expiryTimestamp: getTimeoutTime(autoRedirectDelay),
    onExpire: () => {
      if (onBtnClick) {
        onBtnClick({} as React.MouseEvent<HTMLButtonElement>);
      }
    },
    autoStart: false,
  });

  useEffect(() => {
    if (autoRedirect && status === "success" && onBtnClick) {
      restart(getTimeoutTime(autoRedirectDelay));
    } else {
      pause();
    }

    // Cleanup: pause timer on unmount
    return () => {
      pause();
    };
  }, [autoRedirect, status, onBtnClick, autoRedirectDelay, restart, pause]);

  const countdownDisplay = useMemo(
    () => convertTime(totalSeconds),
    [totalSeconds]
  );

  return (
    <Step className={status === "success" ? "overflow-hidden" : ""}>
      <StepContent className={status === "success" ? "p-0" : ""}>
        {status === "success" && (
          <div className="verification-success-header w-full py-6 px-6 text-center">
            <div className="mb-4 flex justify-center">
              <img
                className="verification-status-icon h-20 w-20"
                alt="Success"
              />
            </div>
            <div className="text-center">
              <h1 className="text-3xl font-bold text-white md:text-2xl sm:text-xl">
                {title}
              </h1>
            </div>
          </div>
        )}

        {status === "failed" && (
          <div className="flex flex-col items-center gap-4 py-4">
            <Icons.failed />
            <div className="text-center text-lg font-semibold">
              <h1>{title}</h1>
            </div>
          </div>
        )}

        <div className={status === "success" ? "verification-success-content flex flex-col items-center px-6 py-6 text-center sm:px-8" : "px-6"}>
          {status === "success" && (
            <>
              <p className="mb-3 text-center text-base text-primary-light md:text-sm">
                {description}
              </p>
              
              {/* Countdown */}
              {autoRedirect && (
                <p className="mb-6 text-sm text-primary">
                  <Trans
                    i18nKey="identity_verification_status.successful.countdown"
                    components={{
                      CountDownSpan: <span className="font-bold" />,
                    }}
                    values={{ countDown: countdownDisplay }}
                  />
                </p>
              )}

              {/* Status Badges */}
              <div className="mb-8 grid w-full max-w-2xl grid-cols-2 gap-4 sm:grid-cols-1 sm:gap-6">
                <div className="verification-status-badge flex items-center gap-2 rounded-lg px-4 py-3">
                  <div className="flex-shrink-0">
                    <img
                      src="/images/verified_success.png"
                      alt="Verified"
                      className="h-10 w-10"
                    />
                  </div>
                  <span className="text-sm font-medium text-primary">
                    {t("identity_verification_status.successful.identity_verified")}
                  </span>
                </div>

                <div className="verification-status-badge flex items-center gap-2 rounded-lg px-4 py-3">
                  <div className="flex-shrink-0">
                    <img
                      src="/images/verified_success.png"
                      alt="Verified"
                      className="h-10 w-10"
                    />
                  </div>
                  <span className="text-sm font-medium text-primary">
                    {t("identity_verification_status.successful.documents_validated")}
                  </span>
                </div>
              </div>
            </>
          )}

          {/* Action Button */}
          {btnLabel && onBtnClick && (
            <Button
              id="success-continue-button"
              className="h-16 w-full"
              onClick={onBtnClick}
            >
              {btnLabel}
            </Button>
          )}
        </div>
      </StepContent>
    </Step>
  );
};
