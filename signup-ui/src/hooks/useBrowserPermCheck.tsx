import { useEffect, useState } from "react";

import { BROWSER_MIN_COMPATIBILITY } from "~constants/browsers";
import { compareVersions } from "~utils/browser";

export const useBrowserPermCheck = () => {
  const [isBrowserCompatible, setIsBrowserCompatible] =
    useState<boolean>(false);
  const [isPermCompatible, setIsPermCompatible] = useState<boolean>(false);

  useEffect(() => {
    const checkBrowserCompatible = () => {
      const userAgent = navigator.userAgent;
      const browserVersionRegex = {
        chrome: /Chrome\/(\d+\.\d+\.\d+\.\d+)/,
        firefox: /Firefox\/(\d+\.\d+)/,
        edge: /Edg\/(\d+\.\d+\.\d+\.\d+)/,
        safari: /Version\/(\d+\.\d+)/,
      };

      for (const [browser, regex] of Object.entries(browserVersionRegex)) {
        const match = userAgent.match(regex);
        if (match) {
          setIsBrowserCompatible(
            compareVersions(match[1], BROWSER_MIN_COMPATIBILITY[browser])
          );
          return;
        }
      }
      setIsBrowserCompatible(false);
    };

    const checkBrowserCameraPermission = async () => {
      try {
        await navigator.mediaDevices.getUserMedia({ video: true });
        let devices = await navigator.mediaDevices.enumerateDevices();
        const videoDevices = devices.filter(
          (device) => device.kind === "videoinput"
        );

        if (
          videoDevices.length > 0 &&
          videoDevices.some((device) => device.deviceId)
        ) {
          setIsPermCompatible(true);
        } else {
          setIsPermCompatible(false);
        }
      } catch (error) {
        setIsPermCompatible(false);
      }
    };
    checkBrowserCompatible();
    checkBrowserCameraPermission();
  }, []);

  return { isBrowserPermCompatible: isBrowserCompatible && isPermCompatible };
};
