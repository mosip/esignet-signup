import { lazy, ReactNode, Suspense } from "react";
import { Route, Routes } from "react-router-dom";

import { SIGNUP_ROUTE } from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";

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
        <Route path={SIGNUP_ROUTE} element={<SignUpPage />} />
        <Route path="*" element={<NotFoundErrorPage />} />
      </Routes>
    </WithSuspense>
  );
};
