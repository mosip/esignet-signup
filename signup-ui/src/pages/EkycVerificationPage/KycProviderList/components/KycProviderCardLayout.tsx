import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Icons } from "~components/ui/icons";
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
  onKycProvidersSelection: () => void;
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
  langMap,
  onKycProvidersSelection,
}: KycProviderCardLayoutProps) => {
  const { i18n } = useTranslation();

  const [providerName, setProviderName] = useState(
    displayName[langMap[i18n.language]]
  );

  useEffect(() => {}, [providerName]);

  useEffect(() => {
    if (langMap) {
      setProviderName(displayName[langMap[i18n.language]]);
    }
  }, [langMap, i18n.language]);

  return (
    <>
      <div
        id={id}
        className={cn(
          "col-span-1 flex cursor-pointer flex-col rounded-lg bg-white p-4",
          selected ? "check-box kyc-box-selected" : "kyc-box"
        )}
        onClick={onKycProvidersSelection}
      >
        <>
          {selected && (
            <div className="check_box">
              <Icons.check className="text-white" />
            </div>
          )}
          <div className="text-md text-justify">
            <div className="kyc-box-icon w-[52px] rounded-md bg-white p-2">
              <img src={logoUrl} alt={id} width="36px" height="36px" />
            </div>
            <div>
              <h3 className="kyc-box-header mt-2.5">{providerName}</h3>
            </div>
          </div>
        </>
      </div>
    </>
  );
};
