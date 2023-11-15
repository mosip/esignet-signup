import { lazy, ReactNode, Suspense, useEffect } from "react";
import { Route, Routes, useNavigate } from "react-router-dom";

import { SIGNUP_ROUTE } from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";
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
        <Route path={SIGNUP_ROUTE} element={<SignUpPage />} />
        <Route path="*" element={<NotFoundErrorPage />} />
      </Routes>
    </WithSuspense>
  );
};
