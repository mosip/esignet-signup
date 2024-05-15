import { useTranslation } from "react-i18next";

import { Button } from "~components/ui/button";
import {
  Step,
  StepContent,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { StepItem, Stepper, Step as StepperStep } from "~components/ui/stepper";
import { cn } from "~utils/cn";

interface StepItemWithContent extends StepItem {
  content: string;
}

const steps = [
  {
    label: "Choose an eKYC provider",
    content:
      "Select an eKYC service provider that aligns with your requirements.",
  },
  {
    label: "Terms & Conditions",
    content: "Review the policy terms & conditions.",
  },
  {
    label: "Pre-verification guide",
    content: "Key instructions for a seamless eKYC experience.",
  },
  {
    label: "Identity verification",
    content:
      "This step verifies the individual’s physical presence during the identity verification process as well as verification of the individual’s identity with their physical ID.",
  },
  {
    label: "Review Consent",
    content:
      "Review and approve consent before sharing with the service provider.",
  },
] as StepItemWithContent[];

export const VerificationSteps = () => {
  const { t } = useTranslation();

  /**
   * Handle cancel button click, show the cancel alert popover
   * @param e event
   */
  const handleCancel = (e: any) => {
    e.preventDefault();
  };

  /**
   * Handle the proceed button click, move forward to video previe page
   * @param e event
   */
  const handleContinue = (e: any) => {
    e.preventDefault();
  };

  return (
    <Step className="2xl:max-w-6xl lg:max-w-3xl">
      <StepHeader className="block px-0 sm:px-[18px] sm:pb-[25px] sm:pt-[33px]">
        <StepTitle className="text-left text-[22px] font-semibold">
          Complete your eKYC verification with the below simple steps
        </StepTitle>
      </StepHeader>
      <StepDivider />
      <StepContent>
        <div className="flex w-full flex-col gap-4">
          <Stepper
            orientation="vertical"
            initialStep={steps.length}
            steps={steps}
            expandVerticalSteps
            size="lg"
            styles={{
              "step-label": cn("font-semibold"),
            }}
          >
            {steps.map((stepProps, index) => {
              return (
                <StepperStep
                  key={stepProps.label}
                  checkIcon={`${index + 1}`}
                  {...stepProps}
                >
                  <div className="mb-4 ml-2 text-sm">{stepProps.content}</div>
                </StepperStep>
              );
            })}
          </Stepper>
        </div>
      </StepContent>
      <StepDivider />
      <StepFooter className="p-5">
        <div className="w-full">
          <div className="float-right flex w-[50%] flex-row items-center justify-center gap-x-4">
            <Button
              id="cancel-tnc-button"
              name="cancel-tnc-button"
              variant="outline"
              className="w-full border-primary p-4 font-semibold text-primary"
              onClick={handleCancel}
            >
              {t("cancel_button")}
            </Button>
            <Button
              id="proceed-tnc-button"
              name="proceed-tnc-button"
              className="w-full p-4 font-semibold"
              onClick={handleContinue}
            >
              {t("proceed_button")}
            </Button>
          </div>
        </div>
      </StepFooter>
    </Step>
  );
};
