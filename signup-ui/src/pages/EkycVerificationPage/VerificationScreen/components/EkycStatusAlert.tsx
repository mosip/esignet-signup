import { Icons } from "~components/ui/icons";
import { Step, StepContent } from "~components/ui/step";

export const EkycStatusAlert = (props: any) => {
  return (
    <Step>
      <StepContent data-testid="ekyc-status">
        <div className="mb-4 flex flex-col items-center gap-4 py-4">
          {props.config.icon === "fail" ? (
            <Icons.failed data-testid="ekyc-fail-icon" />
          ) : props.config.icon === "success" ? (
            <Icons.success data-testid="ekyc-success-icon" />
          ) : null}
          {props.config.header && (
            <div className="text-center text-lg font-semibold">
              {props.config.header}
            </div>
          )}
          <p className="text-center text-gray-500">{props.config.subHeader}</p>
        </div>
        {props.config.footer}
      </StepContent>
    </Step>
  );
};
