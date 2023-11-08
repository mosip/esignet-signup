import { lazy, ReactNode, Suspense } from "react";
import { Route, Routes } from "react-router-dom";

import { SIGNUP_ROUTE, SOMETHING_WENT_WRONG } from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";
import AccountSetupPage from "~pages/AccountSetupPage";
import { SignUpProvider } from "~pages/SignUpPage/SignUpProvider";

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
        <Route
          path={SOMETHING_WENT_WRONG}
          element={<SomethingWentWrongPage />}
        />
        <Route path="*" element={<UnderConstructionPage />} />
      </Routes>
    </WithSuspense>
  );
};
