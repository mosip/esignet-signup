import { lazy, ReactNode, Suspense, useEffect } from "react";
import { Route, Routes, useNavigate } from "react-router-dom";

import { SIGNUP_ROUTE, SOMETHING_WENT_WRONG } from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";
import { setupResponseInterceptor } from "~services/api.service";

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
  const navigate = useNavigate();

  useEffect(() => {
    setupResponseInterceptor(navigate);
  }, [navigate]);

  return (
    <WithSuspense>
      <Routes>
        <Route path={SIGNUP_ROUTE} element={<SignUpPage />} />
        <Route
          path={SOMETHING_WENT_WRONG}
          element={<SomethingWentWrongPage />}
        />
        <Route path="*" element={<UnderConstructionPage />} />
      </Routes>
    </WithSuspense>
  );
};
