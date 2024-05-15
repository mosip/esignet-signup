import { useEffect } from "react";

import { useBrowserPermCheck } from "~hooks/useBrowserPermCheck";

import { UnsupportedBrowserPerm } from "./components/UnsupportedBrowserPerm";

export const LoadingScreen = () => {
  const { isBrowserPermCompatible } = useBrowserPermCheck();
  console.log(
    "🚀 ~ LoadingScreen ~ isBrowserPermCompatible:",
    isBrowserPermCompatible
  );

  if (isBrowserPermCompatible) {
    // return
  }

  return <UnsupportedBrowserPerm />;
};
