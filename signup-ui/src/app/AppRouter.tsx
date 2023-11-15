import { lazy, ReactNode, Suspense } from "react";
import { Route, Routes } from "react-router-dom";

import { SIGNUP_ROUTE, TERMS_CONDITIONS_ROUTE, PRIVACY_POLICY_ROUTE } from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";
import TermsAndPrivacyPage from "~pages/TermsAndPrivacyPage";
import { useTranslation } from "react-i18next";

const SignUpPage = lazy(() => lazyRetry(() => import("~pages/SignUpPage")));
const NotFoundErrorPage = lazy(() =>
  lazyRetry(() => import("~pages/NotFoundErrorPage"))
);

const WithSuspense = ({ children }: { children: ReactNode }) => (
  <Suspense fallback={<div className="h-screen w-screen bg-neutral-100"></div>}>
    {children}
  </Suspense>
);

export const AppRouter = () => {
  const {t} = useTranslation();

  return (
    <WithSuspense>
      <Routes>
        <Route path={SIGNUP_ROUTE} element={<SignUpPage />} />
        <Route path={TERMS_CONDITIONS_ROUTE} element={<TermsAndPrivacyPage title={t("terms_and_conditions_title")} content={t("terms_and_conditions_content")}/>} />
        <Route path={PRIVACY_POLICY_ROUTE} element={<TermsAndPrivacyPage title={t("privacy_and_policy_title")} content={t("privacy_and_policy_content")}/>} />
        <Route path="*" element={<NotFoundErrorPage />} />
      </Routes>
    </WithSuspense>
  );
};
