import { lazy, ReactNode, Suspense, useEffect } from "react";
import { Route, Routes, useNavigate, useLocation, Navigate } from "react-router-dom";

import { ROOT_ROUTE, SIGNUP_ROUTE, SOMETHING_WENT_WRONG, UNDER_CONSTRUCTION } from "~constants/routes";
import Footer from "~components/ui/footer";
import NavBar from "~components/ui/nav-bar";
import { lazyRetry } from "~utils/lazyRetry";

const SignUpPage = lazy(() => lazyRetry(() => import("~pages/SignUpPage")));
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
    <div className="min-h-screen flex flex-col sm:bg-white">
      <NavBar />
      <div className="relative flex flex-grow flex-col">
        <WithSuspense>
          <Routes>
            <Route path={SIGNUP_ROUTE} element={<SignUpPage />} />
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
          </Routes>
        </WithSuspense>
        <Footer />
      </div>
    </div>
  );
};
