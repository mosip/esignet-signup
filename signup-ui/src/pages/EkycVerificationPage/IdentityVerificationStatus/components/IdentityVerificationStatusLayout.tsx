import { MouseEventHandler, useEffect, useMemo } from "react";
import { Trans } from "react-i18next";
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
    <Step>
      <StepContent>
        <div className="status__content">
          {status === "success" ? <Icons.success /> : <Icons.failed />}
          <h1 className="status__title">{title}</h1>
          <p className="status__description">{description}</p>
          {status === "success" && (
            <p className="redirect_countdown">
              <Trans
                i18nKey="identity_verification_status.successful.countdown"
                components={{
                  CountDownSpan: <span className="font-bold" />,
                }}
                values={{ countDown: countdownDisplay }}
              />
            </p>
          )}
        </div>
        {(status === "failed" ||
          (status === "success" && btnLabel && onBtnClick)) && (
          <Button
            id="success-continue-button"
            className="status__btn"
            onClick={onBtnClick}
          >
            {btnLabel}
          </Button>
        )}
      </StepContent>
    </Step>
  );
};
