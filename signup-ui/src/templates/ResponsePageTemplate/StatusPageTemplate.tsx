import { MouseEventHandler } from "react";

import { ReactComponent as FailedIconSvg } from "~assets/svg/failed-icon.svg";
import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { Button } from "~components/ui/button";
import { Step, StepContent, StepHeader, StepTitle } from "~components/ui/step";

interface StatusPageTemplateProps {
  title: string;
  subtitle?: string;
  description: string;
  status: "success" | "error";
  action: string;
  handleAction?: MouseEventHandler<HTMLButtonElement>;
}

export const StatusPageTemplate = ({
  title,
  subtitle,
  description,
  status,
  action,
  handleAction,
}: StatusPageTemplateProps) => {
  return (
    <Step>
      <StepHeader>
        <StepTitle>
          {status === "success" ? <SuccessIconSvg /> : <FailedIconSvg />}
        </StepTitle>
      </StepHeader>
      <StepContent>
        <div className="flex flex-col items-center gap-4 px-4">
          <h1 className="font-bold text-center text-2xl">{title}</h1>
          {subtitle && (
            <h2 className="font-medium text-center text-lg">{subtitle}</h2>
          )}
          <p className="text-center text-sm text-gray-500">{description}</p>
        </div>
        <Button
          className="w-full h-16 my-4"
          variant="secondary"
          onClick={handleAction}
        >
          {action}
        </Button>
      </StepContent>
    </Step>
  );
};

{
  /* <div className="container p-0 max-w-md rounded-[20px] shadow-[0_3px_10px_rgb(0,0,0,0.2)]">
      <div className="flex flex-col items-center justify-center">
        <div className="flex flex-col items-center gap-6 px-4 py-16">
          {status === "success" ? <SuccessIconSvg /> : <FailedIconSvg />}
          <div>
            <h1 className="font-medium text-center text-lg">{title}</h1>
            {subtitle && (
              <h2 className="font-medium text-center text-lg">{subtitle}</h2>
            )}
            <p className="text-center text-sm text-gray-500">{description}</p>
          </div>
          <Button
            className="w-full h-16"
            variant="secondary"
            onClick={handleAction}
          >
            {action}
          </Button>
        </div>
      </div>
    </div> */
}
