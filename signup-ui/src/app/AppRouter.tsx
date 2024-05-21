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
  EKYC_VERIFICATION,
  KYC_PROVIDER_LIST,
  LOADING_SCREEN,
  TERMS_CONDITION,
  VERIFICATION_SCREEN,
  VIDEO_PREVIEW,
  SLOT_CHECKING,
} from "~constants/routes";
import { lazyRetry } from "~utils/lazyRetry";
import { setupResponseInterceptor } from "~services/api.service";

const SignUpPage = lazy(() => lazyRetry(() => import("~pages/SignUpPage")));
const ResetPasswordPage = lazy(() =>
  lazyRetry(() => import("~pages/ResetPasswordPage"))
);
const LandingPage = lazy(() => lazyRetry(() => import("~pages/LandingPage")));
const EkycVerificationPage = lazy(() =>
  lazyRetry(() => import("~pages/EkycVerificationPage"))
);

const KycProviderList = lazy(() =>
  lazyRetry(() => import("~pages/EkycVerificationPage/KycProviderList"))
);
const LoadingScreen = lazy(() => lazyRetry(() => import("~pages/EkycVerificationPage/LoadingScreen")));
const TermsAndCondition = lazy(() =>
  lazyRetry(() => import("~pages/EkycVerificationPage/TermsAndCondition"))
);
const VerificationScreen = lazy(() =>
    lazyRetry(() => import("~pages/EkycVerificationPage/VerificationScreen"))
  );
  const VerificationSteps = lazy(() =>
    lazyRetry(() => import("~pages/EkycVerificationPage/VerificationSteps"))
  );
const SlotChecking = lazy(() =>
  lazyRetry(() => import("~pages/EkycVerificationPage/SlotChecking"))
);
const VideoPreview = lazy(() => lazyRetry(() => import("~pages/EkycVerificationPage/VideoPreview")));
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
          <Route path={EKYC_VERIFICATION} element={<EkycVerificationPage />}>
            <Route path={KYC_PROVIDER_LIST} element={<KycProviderList />} />
            <Route path={TERMS_CONDITION} element={<TermsAndCondition />} />
            <Route path={VIDEO_PREVIEW} element={<VideoPreview />} />
            <Route path={VERIFICATION_SCREEN} element={<VerificationScreen />} />
            <Route path={LOADING_SCREEN} element={<LoadingScreen />} />
            <Route path={EKYC_VERIFICATION} element={<VerificationSteps />} />
            <Route path={SLOT_CHECKING} element={<SlotChecking />} />
          </Route>
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
