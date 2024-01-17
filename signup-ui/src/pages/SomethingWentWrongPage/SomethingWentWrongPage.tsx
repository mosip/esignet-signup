import { getReasonPhrase } from "http-status-codes";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ReactComponent as SomethingWentWrongSvg } from "~assets/svg/something-went-wrong.svg";
import ErrorPageTemplate from "~templates/ErrorPageTemplate";

export const SomethingWentWrongPage = () => {
  const { t } = useTranslation();

  const {
    state: { code },
  } = useLocation();

  return (
    <ErrorPageTemplate
      title={code ? getReasonPhrase(code) : t("something_went_wrong")}
      description={t("something_went_wrong_detail")}
      image={<SomethingWentWrongSvg />}
    />
  );
};
