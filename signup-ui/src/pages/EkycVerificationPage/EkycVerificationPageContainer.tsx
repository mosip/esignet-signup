import { PageLayout } from "~layouts/PageLayout";

import { useSettings } from "~pages/shared/queries";
import LoadingIndicator from "~/common/LoadingIndicator";

import { EkycVerificationPage } from "./EkycVerificationPage";

export const EkycVerificationPageContainer = () => {
  const { data: settings, isLoading } = useSettings();

  return (
    <PageLayout>
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
