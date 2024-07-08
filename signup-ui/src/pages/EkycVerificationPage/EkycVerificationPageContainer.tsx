import { useCallback } from "react";
import { PageLayout } from "~layouts/PageLayout";

import { useSettings } from "~pages/shared/queries";
import LoadingIndicator from "~/common/LoadingIndicator";

import { EkycVerificationPage } from "./EkycVerificationPage";
import {
  EkycVerificationStore,
  isNoBackgroundSelector,
  useEkycVerificationStore,
} from "./useEkycVerificationStore";

export const EkycVerificationPageContainer = () => {
  const { data: settings, isLoading } = useSettings();
  const { isNoBackground } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        isNoBackground: isNoBackgroundSelector(state),
      }),
      []
    )
  );

  return (
    <PageLayout noBackground={isNoBackground ?? false}>
      {isLoading || !settings ? (
        <LoadingIndicator
          message="please_wait"
          msgParam="Loading. Please wait....."
          iconClass="fill-[#eb6f2d]"
          divClass="align-loading-center"
        />
      ) : (
        <EkycVerificationPage settings={settings} />
      )}
    </PageLayout>
  );
};
