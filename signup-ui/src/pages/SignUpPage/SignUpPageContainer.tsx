import { Icons } from "~components/ui/icons";

import { useSettings } from "./queries";
import { SignUpPage } from "./SignUpPage";

export const SignUpPageContainer = () => {
  const { data: settings, isLoading } = useSettings();

  if (isLoading || !settings) {
    return (
      <div className="flex h-[calc(100vh-14vh)] w-full items-center justify-center">
        <Icons.loader className="animate-spin" />
      </div>
    );
  }

  return (
    <div className="relative flex flex-1 items-center justify-center sm:flex-none">
      <img
        className="absolute left-1 top-1 block sm:hidden"
        src="/images/top.png"
        alt="top left background"
      />
      <div className="z-10 w-full">
        <SignUpPage settings={settings} />
      </div>
      <img
        className="absolute bottom-1 right-1 block sm:hidden"
        src="/images/bottom.png"
        alt="bottom right background"
      />
    </div>
  );
};
