import { Icons } from "~components/ui/icons";

import { useSettings } from "./queries";
import { SignUpPage } from "./SignUpPage";
import { SignUpProvider } from "./SignUpProvider";

export const SignUpPageLayout = () => {
  const { data: settings, isLoading } = useSettings();

  if (isLoading || !settings) {
    return (
      <div className="w-full flex h-[calc(100vh-14vh)] items-center justify-center">
        <Icons.loader2 className="animate-spin" />
      </div>
    );
  }

  return (
    <div className="relative w-full flex h-[calc(100vh-14vh)] items-center justify-center">
      <div>
        <img
          className="absolute left-1 top-1"
          src="/images/top.png"
          alt="top left background"
        />
      </div>
      <div className="w-full z-10">
        <SignUpProvider>
          <SignUpPage settings={settings} />
        </SignUpProvider>
      </div>
      <div>
        <img
          className="absolute bottom-1 right-1"
          src="/images/bottom.png"
          alt="bottom right background"
        />
      </div>
    </div>
  );
};
