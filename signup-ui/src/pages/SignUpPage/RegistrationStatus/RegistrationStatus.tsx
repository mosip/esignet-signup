import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import StatusPageTemplate from "~templates/ResponsePageTemplate";
import { getSignInRedirectURL } from "~utils/link";

export const RegistrationStatus = () => {
  const { t } = useTranslation();
  const { hash: fromSingInHash } = useLocation();

  const handleAction = () => {
    window.location.href = getSignInRedirectURL(fromSingInHash);
  };

  return (
    <StatusPageTemplate
      status="success"
      title={t("congratulations")}
      subtitle={t("account_created_successfully")}
      description={t("login_to_proceed")}
      action={!!fromSingInHash ? t("login") : t("okay")}
      handleAction={handleAction}
    />
  );
};
