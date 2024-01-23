import { forwardRef } from "react";
import { useTranslation } from "react-i18next";

const Footer = () => {
  const { t } = useTranslation();

  return (
    <footer className="sm:fixed bg-neutral-50 z-20 bottom-0 left-0 right-0 border-blue-gray-50 flex w-full flex-row flex-wrap items-center justify-center gap-x-1 gap-y-6 border-t py-[11px] text-center">
      <span className="text-sm text-[hsl(0,0%,53.7%)]">
        {t("footer.powered_by")}
      </span>
      <img
        src="/images/footer_logo.png"
        alt="footer logo"
        className="h-[25px]"
      />
    </footer>
  );
};

export default forwardRef(Footer);
