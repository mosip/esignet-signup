import { PageLayout } from "~layouts/PageLayout";

import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";
import { useSettings } from "~pages/shared/queries";

import { ResetPasswordPage } from "./ResetPasswordPage";

export const ResetPasswordPageContainer = () => {
  const { data: settings, isLoading } = useSettings();

  if (isLoading || !settings) {
    return (
      <PageLayout>
        <Step>
          <StepContent className="flex h-40 items-center justify-center">
            <Icons.loader className="animate-spin text-primary" />
          </StepContent>
        </Step>
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <ResetPasswordPage settings={settings} />
    </PageLayout>
  );
};
