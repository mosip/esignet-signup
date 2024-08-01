import { useTranslation } from "react-i18next";

import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";

export const IdentityVerificationStatusLoader = () => {
  const { t } = useTranslation();

  return (
    <Step>
      <StepContent>
        <div className="status-loader__content">
          <Icons.loader className="status-loader__icon" />
          <div>
            <h1 className="status-loader__title">
              {t("identity_verification_status.loading_title")}
            </h1>
          </div>
        </div>
      </StepContent>
    </Step>
  );
};
