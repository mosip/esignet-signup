import { useTranslation } from "react-i18next";

import StatusPageTemplate from "~templates/ResponsePageTemplate";

export const RegistrationStatus = () => {
  const { t } = useTranslation();

  return (
    <StatusPageTemplate
      status="success"
      title={t("congratulations")}
      subtitle={t("account_created_successfully")}
      description={t("login_to_proceed")}
      action={t("login")}
    />
  );
};
