import { forwardRef, ReactNode } from "react";
import { useTranslation } from "react-i18next";

const Footer = ({ i18nKeyPrefix = "footer" }) => {
  const footerCheck = process.env.REACT_APP_FOOTER === "true";

  const { t } = useTranslation("translation", {
    keyPrefix: i18nKeyPrefix,
  });

  return (
    <>
      {footerCheck && (
        <footer className="footer-container flex w-full flex-row flex-wrap items-center justify-center gap-y-6 gap-x-1 border-t border-blue-gray-50 text-center">
          <span className="footer-text">{t("powered_by")}</span>
          <img className="footer-brand-logo" alt="footerLogo" />
        </footer>
      )}
    </>
  );
};

export default forwardRef(Footer);