import { useTranslation } from "react-i18next";

import { ReactComponent as PageUnderConstructionSvg } from "~assets/svg/page-under-construction.svg";
import ErrorPageTemplate from "~templates/ErrorPageTemplate";

export const UnderConstructionPage = () => {
  const { t } = useTranslation();

  return (
    <ErrorPageTemplate
      title={t("page_under_construction")}
      description={t("page_under_construction_detail")}
      image={<PageUnderConstructionSvg />}
    />
  );
};
