import Footer from "~components/ui/footer";
import { Icons } from "~components/ui/icons";
import NavBar from "~components/ui/nav-bar";

import { useSettings } from "./queries";
import { SignUpPage } from "./SignUpPage";
import { SignUpProvider } from "./SignUpProvider";

export const SignUpPageLayout = () => {
  const { data: settings, isLoading } = useSettings();

  if (isLoading || !settings) {
    return (
      <div className="h-screen flex items-center justify-center">
        <Icons.loader2 className="animate-spin" />
      </div>
    );
  }

  return (
    <>
      <NavBar />
      <div className="flex flex-col">
        <div className="">
          <img
            className="left-1 top-1"
            src="/images/top.png"
            alt="top left background"
          />
        </div>
        <SignUpProvider>
          <SignUpPage settings={settings} />
        </SignUpProvider>
        <div className="flex justify-end">
          <img
            className="left-1 top-1"
            src="/images/bottom.png"
            alt="bottom right background"
          />
        </div>
      </div>
      <Footer />
    </>
  );
};
