import { PageLayout } from "~layouts/PageLayout";

import { Icons } from "~components/ui/icons";
import { useSettings } from "~pages/shared/queries";

import { EkycVerificationPage } from "./EkycVerificationPage";

export const EkycVerificationPageContainer = () => {
  const { data: settings, isLoading } = useSettings();

  if (isLoading || !settings) {
    return (
      <div className="flex h-[calc(100vh-14vh)] w-full items-center justify-center">
        <Icons.loader className="animate-spin" />
      </div>
    );
  }

  return (
    <PageLayout>
      <EkycVerificationPage settings={settings}/>
    </PageLayout>
  );
};
