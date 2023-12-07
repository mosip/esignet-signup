import { Icons } from "~components/ui/icons";

import { useSettings } from "./queries";
import { SignUpPage } from "./SignUpPage";

export const SignUpPageContainer = () => {
  const { data: settings, isLoading } = useSettings();

  if (isLoading || !settings) {
    return (
      <div className="w-full flex h-[calc(100vh-14vh)] items-center justify-center">
        <Icons.loader className="animate-spin" />
      </div>
    );
  }

  return (
    <div className="relative flex-1 flex items-center justify-center">
      <img
        className="absolute left-1 top-1"
        src="/images/top.png"
        alt="top left background"
      />
      <div className="w-full z-10">
        <SignUpPage settings={settings} />
      </div>
      <img
        className="absolute bottom-1 right-1"
        src="/images/bottom.png"
        alt="bottom right background"
      />
    </div>
  );
};
