import { Step, StepContent } from "~components/ui/step";
import LoadingIndicator from "~/common/LoadingIndicator";

export const IdentityVerificationStatusLayoutPlaceholder = () => {
  return (
    <Step>
      <StepContent>
        <div className="status__content">
          <LoadingIndicator />
        </div>
      </StepContent>
    </Step>
  );
};
