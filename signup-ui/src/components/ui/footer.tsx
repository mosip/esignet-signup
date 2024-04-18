import { forwardRef } from "react";
import { useTranslation } from "react-i18next";

const Footer = () => {
  const { t } = useTranslation();

  return (
    <footer className="footer-container border-blue-gray-50 bottom-0 left-0 right-0 z-20 flex w-full flex-row flex-wrap items-center justify-center gap-x-1 gap-y-6 border-t bg-neutral-50 py-[11px] text-center sm:fixed">
      <span className="">{t("footer.powered_by")}</span>
      <img className="footer-brand-logo" alt={t("logo_alt")} />
    </footer>
  );
};

export default forwardRef(Footer);
