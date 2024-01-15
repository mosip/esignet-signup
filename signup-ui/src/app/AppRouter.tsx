import { lazy, ReactNode, Suspense, useEffect } from "react";
import { AppLayout } from "~layouts/AppLayout";
import {
  Navigate,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from "react-router-dom";

import {
  RESET_PASSWORD,
  ROOT_ROUTE,
  SIGNUP_ROUTE,
  SOMETHING_WENT_WRONG,
  UNDER_CONSTRUCTION,
} from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";

const SignUpPage = lazy(() => lazyRetry(() => import("~pages/SignUpPage")));
const ResetPasswordPage = lazy(() =>
  lazyRetry(() => import("~pages/ResetPasswordPage"))
);
const LandingPage = lazy(() => lazyRetry(() => import("~pages/LandingPage")));
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
  const { hash: fromSignInHash } = useLocation();
  const REDIRECT_ROUTE = `${ROOT_ROUTE}${fromSignInHash}`;

  useEffect(() => {
    setupResponseInterceptor(navigate);
  }, [navigate]);

  return (
    <WithSuspense>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path={SIGNUP_ROUTE} element={<SignUpPage />} />
          <Route path={RESET_PASSWORD} element={<ResetPasswordPage />} />
          <Route
            path={SOMETHING_WENT_WRONG}
            element={<SomethingWentWrongPage />}
          />
          <Route
            path={UNDER_CONSTRUCTION}
            element={<UnderConstructionPage />}
          />
          <Route path={ROOT_ROUTE} element={<LandingPage />} />
          <Route path="*" element={<Navigate to={REDIRECT_ROUTE} />} />
        </Route>
      </Routes>
    </WithSuspense>
  );
};
