import { useTranslation } from "react-i18next";

const LandingFooter = () => {
  const { t } = useTranslation();

  return (
    <footer className="bottom-0 left-0 right-0 z-20 flex w-full flex-row items-center justify-between gap-4 border-t border-gray-200 bg-neutral-50 px-[4rem] py-[15px] sm:fixed sm:flex-col sm:px-4">
      {/* Left side - Copyright */}
      <div className="flex items-center gap-2">
        <img 
          src="/images/veridonia_emblem.svg" 
          alt="Veridonia Logo" 
          className="h-6 w-6"
        />
        <span className="text-sm text-primary">
          {t("copyright")}
        </span>
      </div>

      {/* Right side - Links */}
      <div className="flex flex-wrap items-center justify-center gap-4 text-sm">
        <a
          target="_blank"
          href="https://www.example.com/" 
          className="text-primary hover:text-primary/80 hover:underline"
        >
          {t("privacy_policy")}
        </a>
        <a 
          target="_blank"
          href="https://www.example.com/" 
          className="text-primary hover:text-primary/80 hover:underline"
        >
          {t("terms_of_service")}
        </a>
        <a 
          target="_blank"
          href="https://www.example.com/" 
          className="text-primary hover:text-primary/80 hover:underline"
        >
          {t("accessibility")}
        </a>
      </div>
    </footer>
  );
};

export default LandingFooter;
