import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { cn } from "~utils/cn";

interface KycProviderCardLayoutProps {
  id: string;
  logoUrl: string;
  displayName: { [key: string]: string };
  processType: string;
  active: boolean;
  retryOnFailure: boolean;
  resumeOnSuccess: boolean;
  selected: boolean;
  langMap: { [key: string]: string };
}

export const KycProviderCardLayout = ({
  id,
  logoUrl,
  displayName,
  processType,
  active,
  retryOnFailure,
  resumeOnSuccess,
  selected,
  langMap
}: KycProviderCardLayoutProps) => {
  const { i18n } = useTranslation();

  const [providerName, setProviderName] = useState(displayName[langMap[i18n.language]]);

  useEffect(() => {}, [providerName]);

  useEffect(() => {
    if(langMap) {
      setProviderName(displayName[langMap[i18n.language]]);
    }
  }, [langMap, i18n.language]);

  return (
    <>
      <div id={id}>
        <div
          className={cn(
            "container max-w-lg cursor-pointer rounded-lg bg-white p-4 shadow sm:max-w-none sm:rounded-lg sm:shadow border-2 border-solid border-transparent",
            selected ? "check-box border-[#EB6F2D]" : ""
          )}
        >
          <div className="flex flex-col justify-start">
            <div className="w-[52px] rounded-md bg-white p-2 shadow-lg">
              <img src={logoUrl} alt={id} width="36px" height="36px" />
            </div>
            <div className="mt-2.5 text-[16px] font-semibold leading-5 tracking-normal text-[#2B3840]">
              {providerName}
            </div>
            <div className="mt-3">
              <div className="text-[14px] font-semibold leading-6 tracking-normal text-[#2B3840]">
                Supported Ids:
              </div>
              <div>Foundation 1, Foundation 2</div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};
