import { MouseEventHandler } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";

interface IdentityVerificationStatusLayoutProps {
  status: "success" | "failed";
  title: string;
  description: string;
  onActionClick?: MouseEventHandler<HTMLButtonElement> | undefined;
}

export const IdentityVerificationStatusLayout = ({
  status,
  title,
  description,
  onActionClick,
}: IdentityVerificationStatusLayoutProps) => {
  const { t } = useTranslation();

  return (
    <Step>
      <StepContent>
        <div className="status__content">
          {status === "success" ? <Icons.success /> : <Icons.failed />}
          <h1 className="status__title">{title}</h1>
          <p className="status__description">{description}</p>
        </div>
        <Button
          id="success-continue-button"
          className="status__btn"
          onClick={onActionClick}
        >
          {t("okay")}
        </Button>
      </StepContent>
    </Step>
  );
};
