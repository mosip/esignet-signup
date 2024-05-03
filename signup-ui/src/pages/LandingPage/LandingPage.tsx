import { PageLayout } from "~layouts/PageLayout";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { ReactComponent as SomethingWentWrongSvg } from "~assets/svg/something-went-wrong.svg";
import { RESET_PASSWORD, SIGNUP_ROUTE } from "~constants/routes";
import { Button } from "~components/ui/button";

export const LandingPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { hash: fromSignInHash } = useLocation();

  const handleResetPassword = (e: any) => {
    e.preventDefault();
    navigate(`${RESET_PASSWORD}${fromSignInHash}`);
  };

  const handleRegister = (e: any) => {
    e.preventDefault();
    navigate(`${SIGNUP_ROUTE}${fromSignInHash}`);
  };

  return (
    <PageLayout
      className="h-[calc(100vh-13vh)] w-full items-center justify-center p-16 px-32 sm:px-[30px]"
      childClassName="h-full"
    >
      <div className="flex h-full w-full flex-col items-center justify-center gap-y-8 rounded-xl bg-white shadow-lg md:shadow-none">
        <SomethingWentWrongSvg />
        <div className="flex flex-col items-center gap-y-2">
          <h1 className="text-center text-2xl">{t("landing_page_title")}</h1>
          <p className="text-center text-gray-500">
            {t("landing_page_description")}
          </p>
        </div>
        <div className="flex w-full flex-row items-center justify-center gap-x-2 sm:flex-col">
          <Button
            className="h-[52px] w-[250px] border-[2px] border-primary bg-white text-primary hover:text-primary/80 sm:mb-3 sm:w-full"
            id="reset-password-button"
            name="reset-password-button"
            variant="outline"
            onClick={handleResetPassword}
          >
            {t("reset_password")}
          </Button>
          <Button
            className="h-[52px] w-[250px] sm:w-full"
            id="register-button"
            name="register-button"
            onClick={handleRegister}
          >
            {t("register")}
          </Button>
        </div>
      </div>
    </PageLayout>
  );
};
