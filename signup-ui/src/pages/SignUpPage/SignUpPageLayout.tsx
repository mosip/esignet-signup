import { Icons } from "~components/ui/icons";

import { useSettings } from "./queries";
import { SignUpPage } from "./SignUpPage";

export const SignUpPageLayout = () => {
  const { data: settings, isLoading } = useSettings();

  if (isLoading || !settings) {
    return (
      <div className="h-screen flex items-center justify-center">
        <Icons.loader2 className="animate-spin" />
      </div>
    );
  }

  return <SignUpPage settings={settings} />;
};
