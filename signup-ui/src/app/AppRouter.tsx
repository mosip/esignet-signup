import { lazy, ReactNode, Suspense, useEffect } from "react";
import { Route, Routes, useNavigate } from "react-router-dom";

import { ACCOUNT_SETUP_ROUTE, SIGNUP_ROUTE, TERMS_CONDITIONS_ROUTE, PRIVACY_POLICY_ROUTE } from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";
import AccountSetupPage from "~pages/AccountSetupPage";
import TermsAndPrivacyPage from "~pages/TermsAndPrivacyPage";
import { SignUpProvider } from "~pages/SignUpPage/SignUpProvider";
import { useTranslation } from "react-i18next";

const SignUpPage = lazy(() => lazyRetry(() => import("~pages/SignUpPage")));
const UnderConstructionPage = lazy(() =>
  lazyRetry(() => import("~pages/UnderConstructionPage"))
);
const SomethingWentWrongPage = lazy(() =>
  lazyRetry(() => import("~pages/SomethingWentWrongPage"))
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
        <Route
          path={SIGNUP_ROUTE}
          element={
            <SignUpProvider>
              <SignUpPage />
            </SignUpProvider>
          }
        />
        <Route path={ACCOUNT_SETUP_ROUTE} element={<AccountSetupPage />} />
        <Route path={TERMS_CONDITIONS_ROUTE} element={<TermsAndPrivacyPage title={t("term_title")} content={t("term_content")}/>} />
        <Route path={PRIVACY_POLICY_ROUTE} element={<TermsAndPrivacyPage title={t("privacy_title")} content={t("privacy_content")}/>} />
        <Route path="*" element={<NotFoundErrorPage />} />
      </Routes>
    </WithSuspense>
  );
};
