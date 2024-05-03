import { useTranslation } from "react-i18next";
import { useNavigate, useLocation } from "react-router-dom";

import { SIGNUP_ROUTE, RESET_PASSWORD } from "~constants/routes";
import { ReactComponent as SomethingWentWrongSvg } from "~assets/svg/something-went-wrong.svg";
import { Button } from "~components/ui/button";

export const LandingPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { hash: fromSignInHash } = useLocation();

  const handleResetPassword = (e: any) => {
    e.preventDefault();
    navigate(`${RESET_PASSWORD}${fromSignInHash}`);
  }
  
  const handleRegister = (e: any) => {
    e.preventDefault();
    navigate(`${SIGNUP_ROUTE}${fromSignInHash}`);
  }

  return (
    <div className="flex h-[calc(100vh-14vh)] w-full items-center justify-center p-16 px-32 sm:px-[30px]">
      <div className="h-full bg-white flex w-full flex-col items-center justify-center gap-y-8 rounded-xl shadow-lg md:shadow-none">
        <SomethingWentWrongSvg/>
        <div className="flex flex-col items-center gap-y-2">
          <h1 className="text-center text-2xl">{t("landing_page_title")}</h1>
          <p className="text-center text-gray-500">{t("landing_page_description")}</p>
        </div>
        <div className="flex flex-row sm:flex-col items-center gap-x-2 w-full items-center justify-center">
          <Button className="h-[52px] w-[200px] border-primary border-[2px] text-primary hover:text-primary/80 bg-white sm:mb-3 sm:w-full"
            variant="outline"
            onClick={handleResetPassword}>
            {t("reset_password")}
          </Button>
          <Button className="h-[52px] w-[200px] sm:w-full" onClick={handleRegister}>
            {t("register")}
          </Button>
        </div>
      </div>
    </div>
  );
};
