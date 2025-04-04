import { useCallback, useEffect, useRef, useState } from "react";
import { Detector, PollingConfig } from "react-detect-offline";
import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import { FormControl, FormField, FormItem } from "~components/ui/form";
import { SearchBox } from "~components/ui/search-box";
import {
  Step,
  StepContent,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { useL2Hash } from "~hooks/useL2Hash";
import langConfigService from "~services/langConfig.service";
import { DefaultEkyVerificationProp } from "~typings/types";
import LoadingIndicator from "~/common/LoadingIndicator";

import {
  EkycVerificationStep,
  EkycVerificationStore,
  hashCodeSelector,
  kycProviderSelector,
  kycProvidersListSelector,
  setKycProviderSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { KycProviderCardLayout } from "./components/KycProviderCardLayout";

const POLLING_BASE_URL =
  process.env.NODE_ENV === "development"
    ? process.env.REACT_APP_API_BASE_URL
    : window.origin + "/v1/signup";

export const KycProviderList = ({
  cancelPopup,
  settings,
}: DefaultEkyVerificationProp) => {
  const { i18n, t } = useTranslation("translation", {
    keyPrefix: "kyc_provider",
  });

  const { state } = useL2Hash();
  const pollingUrl = POLLING_BASE_URL + "/actuator/health";

   const pollingConfig: PollingConfig = {
        timeout: settings?.configs?.["offline.polling.timeout"] ?? 5000,
        interval: settings?.configs?.["offline.polling.interval"] ?? 10000,
        enabled: settings?.configs?.["offline.polling.enabled"] ?? true,
        url: settings?.configs?.["offline.polling.url"] ?? pollingUrl
      };

  const { setStep, setKycProvider, kycProvider, providerListStore } =
    useEkycVerificationStore(
      useCallback(
        (state: EkycVerificationStore) => ({
          setStep: setStepSelector(state),
          setKycProvider: setKycProviderSelector(state),
          kycProvider: kycProviderSelector(state),
          providerListStore: kycProvidersListSelector(state),
        }),
        []
      )
    );

  const [cancelButton, setCancelButton] = useState<boolean>(false);
  const [kycProvidersList, setKycProvidersList] = useState<any>([]);
  const [selectedKycProvider, setSelectedKycProvider] = useState<any>(null);
  const searchTextRef = useRef<HTMLInputElement | null>(null);
  const [langMap, setLangMap] = useState({} as { [key: string]: string });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  /**
   * Handle the proceed button click, move forward to video preview page
   * @param e event
   */
  const handleContinue = (e: any) => {
    e.preventDefault();
    setStep(EkycVerificationStep.TermsAndCondition);
  };

  /**
   * Handle cancel button click, show the cancel alert popover
   * @param e event
   */
  const handleCancel = (e: any) => {
    e.preventDefault();
    if (kycProvidersList === null || kycProvidersList.length === 0) {
      window.onbeforeunload = null;
      window.location.href = `${settings?.configs["esignet-consent.redirect-url"]}?key=${state}&error=no_ekyc_provider`;
    } else {
      setCancelButton(true);
    }
  };

  /**
   * Handle the stay button click, close the cancel alert popover
   */
  const handleStay = () => {
    setCancelButton(false);
  };

  /**
   * Select KycProvider card and highlight it
   * Also set the selected kycProvider & adding
   * it in the ekyc verification store
   * @param e kycProvider detail
   */
  const selectingKycProviders = (e: any) => {
    setKycProvider(e);
    setSelectedKycProvider(e.id);
  };

  /**
   * Filter the kyc providers list based on the search text
   */
  const filterKycProvidersList = () => {
    const val = searchTextRef.current?.value;
    if (providerListStore === null || providerListStore.length === 0) return;
    if (val) {
      const filteredList = providerListStore.filter((item: any) => {
        const displayName =
          item.displayName[langMap[i18n.language]] ?? item.displayName["@none"];
        return displayName.toLowerCase().includes(val.toLowerCase());
      });
      setKycProvidersList(filteredList);
    } else {
      setKycProvidersList(providerListStore);
    }
  };

  /**
   * Clear the search box text
   */
  const clearSearchText = () => {
    if (searchTextRef?.current?.value) {
      searchTextRef.current.value = "";
      setKycProvidersList(providerListStore);
    }
  };

  useEffect(() => {
    // on language change, clear the search text
    // restore all kyc providers
    i18n.on("languageChanged", () => {
      clearSearchText();
    });

    // getting the lang code mapping
    langConfigService.getLangCodeMapping().then((langMap: any) => {
      setLangMap(langMap);
    });

    // if kycProvider is already set, then move to the next step
    if (kycProvider !== null) {
      setStep(EkycVerificationStep.TermsAndCondition);
    }

    setKycProvidersList(providerListStore);
    setIsLoading(false);
  }, []);

  return (
    <>
      {cancelPopup({ cancelButton, handleStay })}
      {isLoading && (
        <LoadingIndicator
          message="please_wait"
          msgParam="Loading. Please wait....."
          iconClass="loading-indicator"
          divClass="align-loading-center"
        />
      )}
      {!isLoading && (
        <div className="my-4 flex flex-row items-stretch justify-center gap-x-1">
          <Step className="mx-10 max-w-[70rem] md:rounded-2xl md:shadow sm:mx-0 sm:rounded-2xl sm:shadow">
            <StepHeader className="p-5 sm:pb-[25px] sm:pt-[33px]">
              <StepTitle className="relative flex w-full flex-row items-center justify-between text-base font-semibold md:flex-col md:justify-center">
                <div className="kyc-header w-full" id="kyc-provider-header">
                  {t("header")}
                </div>
                {providerListStore && providerListStore.length > 2 && (
                  <div
                    id="search-box"
                    className="2xl:w-6/12 xl:w-6/12 md:mt-2 md:w-full sm:w-full"
                  >
                    <FormField
                      name="username"
                      render={(field) => (
                        <FormItem className="space-y-0">
                          <div className="space-y-2">
                            <FormControl>
                              <SearchBox
                                id="username"
                                placeholder={t("search_placeholder")}
                                className="py-6"
                                searchRef={searchTextRef}
                                onChange={filterKycProvidersList}
                                onKeyDown={(event:any) => {
                                  if (event.key === "Enter" || event.keyCode === 13) {
                                    event.preventDefault(); // Prevent default action for Enter key
                                  }
                                }}
                              />
                            </FormControl>
                          </div>
                        </FormItem>
                      )}
                    />
                  </div>
                )}
              </StepTitle>
            </StepHeader>
            <StepDivider />
            <StepContent className="scrollable-div !h-[408px] p-5 text-sm">
              <div className="grid grid-cols-3 items-stretch gap-x-4 gap-y-5 md:grid-cols-2 sm:grid-cols-1 sm:gap-y-3.5">
                {kycProvidersList?.map((keyInfo: any) => (
                  <KycProviderCardLayout
                    key={keyInfo.id}
                    {...keyInfo}
                    selected={selectedKycProvider === keyInfo.id}
                    langMap={langMap}
                    onKycProvidersSelection={() =>
                      selectingKycProviders(keyInfo)
                    }
                  />
                ))}
                {(!kycProvidersList || kycProvidersList.length === 0) && (
                  <div>{t("no_kyc_provider")}</div>
                )}
              </div>
            </StepContent>
            <StepDivider />
            <StepFooter className="p-5">
              <div className="flex w-full flex-row items-center justify-end gap-x-4 sm:justify-center">
                <Button
                  id="cancel-preview-button"
                  name="cancel-preview-button"
                  variant="cancel_outline"
                  className="max-w-max px-[6rem] font-semibold sm:px-[2rem] xs:px-[1.5rem]"
                  onClick={handleCancel}
                >
                  {t("cancel_button")}
                </Button>
                <Detector
                  polling={pollingConfig}
                  render={({ online }) => (
                    <Button
                      id="proceed-preview-button"
                      name="proceed-preview-button"
                      className="max-w-max px-[6rem] font-semibold sm:px-[2rem] xs:px-[1.5rem]"
                      onClick={handleContinue}
                      disabled={!online || !selectedKycProvider}
                    >
                      {t("proceed_button")}
                    </Button>
                  )}
                />
              </div>
            </StepFooter>
          </Step>
        </div>
      )}
    </>
  );
};