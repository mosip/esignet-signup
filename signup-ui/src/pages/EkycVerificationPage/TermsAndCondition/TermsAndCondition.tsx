import { useCallback, useEffect, useState } from "react";
import purify from "dompurify";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { ActionMessage } from "~components/ui/action-message";
import { Button } from "~components/ui/button";
import { Checkbox } from "~components/ui/checkbox";
import {
  Step,
  StepAlert,
  StepContent,
  StepDescription,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { useTermsAndConditions } from "~pages/shared/queries";
import langConfigService from "~services/langConfig.service";
import { DefaultEkyVerificationProp } from "~typings/types";
import LoadingIndicator from "~/common/LoadingIndicator";

import {
  EkycVerificationStep,
  EkycVerificationStore,
  kycProviderSelector,
  setCriticalErrorSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";

export const TermsAndCondition = ({ cancelPopup, settings }: DefaultEkyVerificationProp) => {
  const { i18n, t } = useTranslation("translation", {
    keyPrefix: "terms_and_conditions",
  });

  const { setStep, setCriticalError, kycProvider } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
        kycProvider: kycProviderSelector(state),
      }),
      []
    )
  );

  const [agreeTerms, setAgreeTerms] = useState<boolean>(false);
  const [cancelButton, setCancelButton] = useState<boolean>(false);
  const [tncMessage, setTncMessage] = useState<string>("");
  const [termsAndCondition, setTermsAndCondition] = useState<any>(null);
  const [langMap, setLangMap] = useState<any>({});

  /**
   * Handle the proceed button click, move forward to video preview page
   * @param e event
   */
  const handleContinue = (e: any) => {
    e.preventDefault();
    if (agreeTerms) {
      setStep(EkycVerificationStep.VideoPreview);
    }
  };

  /**
   * Handle cancel button click, show the cancel alert popover
   * @param e event
   */
  const handleCancel = (e: any) => {
    e.preventDefault();
    setCancelButton(true);
  };

  /**
   * Handle the change of the agree with terms & condition checkbox
   */
  const changeAgreeTerms = useCallback((checked: boolean) => {
    setAgreeTerms(checked);
  }, []);

  /**
   * Handle the stay button click, close the cancel alert popover
   */
  const handleStay = () => {
    setCancelButton(false);
  };

  // sanitizing the html content, through dompurify
  // then passing it in the dangerouslySetInnerHTML
  const sanitizeMsg = (message: string) => {
    return {
      __html: purify.sanitize(message),
    };
  };

  const {
    data: tnc,
    isLoading,
    isSuccess,
  } = useTermsAndConditions(kycProvider ? kycProvider.id : "");

  // checking if kycProvider is set or not,
  // if not then return to kycProviderList page
  useEffect(() => {
    if (kycProvider === null) {
      setStep(EkycVerificationStep.KycProviderList);
    }
    langConfigService.getLangCodeMapping().then((langMap) => {
      setLangMap(langMap);
    });
  }, []);

  useEffect(() => {
    if (isSuccess) {
      if (tnc.errors === null || tnc.errors.length === 0) {
        setTermsAndCondition(tnc.response["terms&Conditions"]);
      }
    }
  }, [isSuccess]);

  useEffect(() => {
    if (termsAndCondition) {
      const currLang = langMap[i18n.language];
      setTncMessage(termsAndCondition[currLang]);
    }
  }, [termsAndCondition, i18n.language, langMap]);

  return (
    <>
      {cancelPopup({ cancelButton, handleStay })}
      {isLoading && <LoadingIndicator message="please_wait" msgParam="Loading. Please wait....." iconClass="fill-[#eb6f2d]" />}
      {!isLoading && (
        <div className="m-3 flex flex-row justify-center">
        <Step className="my-5 max-w-[644px] md:rounded-2xl md:shadow sm:rounded-2xl sm:shadow">
          <StepHeader className="px-0 py-5 sm:pb-[25px] sm:pt-[33px]">
            <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-base font-semibold">
              <div
                className="ml-5 w-full text-[22px] font-semibold"
                id="tnc-header"
              >
                {t("header")}
              </div>
            </StepTitle>
            <StepDescription className="w-full text-start tracking-normal">
              <div className="ml-5 text-muted-neutral-gray" id="tnc-sub-header">
                {t("sub_header")}
              </div>
            </StepDescription>
          </StepHeader>
          <StepDivider />
          <StepContent className="px-6 py-5">
            {!termsAndCondition && <div>{t("failed_to_load")}</div>}
            {termsAndCondition && (
              <div
                id="tnc-content"
                className="scrollable-div flex text-justify text-sm sm:p-0"
                dangerouslySetInnerHTML={sanitizeMsg(tncMessage)}
              ></div>
            )}
          </StepContent>
          <StepAlert>
            <ActionMessage className="justify-start bg-[#FFF6F2]">
              <Checkbox
                id="consent-button"
                checked={agreeTerms}
                onCheckedChange={changeAgreeTerms}
                disabled={!termsAndCondition}
                className="h-5 w-5 rounded-[2px] text-white data-[state=checked]:border-primary data-[state=checked]:bg-primary"
              />
              <p className="ml-2 truncate text-xs font-bold">
                {t("agree_text")}
              </p>
            </ActionMessage>
          </StepAlert>
          <StepDivider />
          <StepFooter className="p-5">
            <div className="flex w-full flex-row items-center justify-center gap-x-4">
              <Button
                id="cancel-tnc-button"
                name="cancel-tnc-button"
                variant="cancel_outline"
                className="w-full p-4 font-semibold"
                onClick={handleCancel}
              >
                {t("cancel_button")}
              </Button>
              <Button
                id="proceed-tnc-button"
                name="proceed-tnc-button"
                className="w-full p-4 font-semibold"
                onClick={handleContinue}
                disabled={!agreeTerms}
              >
                {t("proceed_button")}
              </Button>
            </div>
          </StepFooter>
        </Step>
      </div>
      )}
    </>
  );
};
