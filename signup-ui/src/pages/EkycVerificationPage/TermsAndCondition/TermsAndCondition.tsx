import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";

import { SIGNUP_ROUTE, VIDEO_PREVIEW } from "~constants/routes";
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
import { getSignInRedirectURL } from "~utils/link";
import { useTermsAndConditions } from "~pages/shared/queries";

import { CancelAlertPopover } from "../CancelAlertPopover";

export const TermsAndCondition = () => {
  const { i18n, t } = useTranslation("translation", {
    keyPrefix: "terms_and_conditions",
  });

  const { hash: fromSignInHash } = useLocation();

  const navigate = useNavigate();
  const [agreeTerms, setAgreeTerms] = useState<boolean>(false);
  const [cancelButton, setCancelButton] = useState<boolean>(false);

  /**
   * Handle the proceed button click, move forward to video previe page
   * @param e event
   */
  const handleContinue = (e: any) => {
    e.preventDefault();
    if (agreeTerms) {
      navigate(VIDEO_PREVIEW);
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

  /**
   * Handle the dismiss button click, redirect to relying party page
   */
  const handleDismiss = () => {
    window.location.href = getSignInRedirectURL(
      "http://localhost:5000",
      fromSignInHash,
      SIGNUP_ROUTE
    );
  };

  const { data: tnc, isLoading, isSuccess } = useTermsAndConditions();

  return (
    <>
      {cancelButton && (
        <CancelAlertPopover
          description={"description"}
          handleStay={handleStay}
          handleDismiss={handleDismiss}
        />
      )}
      <Step className="max-w-[644px]">
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
          {isLoading && <div>Still Loading</div>}
          {!isLoading && !isSuccess && <div>Failed to Load</div>}
          {isSuccess && (
            <div
              id="tnc-content"
              className="scrollable-div flex text-justify text-sm sm:p-0"
              dangerouslySetInnerHTML={{
                __html: tnc.response?.message ?? "Hello",
              }}
            ></div>
          )}
        </StepContent>
        <StepAlert>
          <ActionMessage className="justify-start bg-[#FFF6F2]">
            <Checkbox
              id="consent-button"
              checked={agreeTerms}
              onCheckedChange={changeAgreeTerms}
              className="h-5 w-5 rounded-[2px] text-white data-[state=checked]:border-primary data-[state=checked]:bg-primary"
            />
            <p className="ml-2 truncate text-xs font-bold">{t("agree_text")}</p>
          </ActionMessage>
        </StepAlert>
        <StepDivider />
        <StepFooter className="p-5">
          <div className="flex w-full flex-row items-center justify-center gap-x-4">
            <Button
              id="cancel-tnc-button"
              name="cancel-tnc-button"
              variant="outline"
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
    </>
  );
};
