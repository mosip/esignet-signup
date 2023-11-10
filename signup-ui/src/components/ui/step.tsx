import * as React from "react";

import { cn } from "~utils/cn";

const Step = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "px-0 container max-w-lg rounded-[20px] bg-white shadow-[0_4px_10px_rgb(0,0,0,0.1)]",
      className
    )}
    {...props}
  />
));
Step.displayName = "Step";

const StepHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col items-center space-y-1.5 p-6", className)}
    {...props}
  />
));
StepHeader.displayName = "StepHeader";

const StepTitle = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-3xl font-medium leading-none tracking-tight",
      className
    )}
    {...props}
  />
));
StepTitle.displayName = "StepTitle";

const StepDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-center text-gray-500", className)}
    {...props}
  />
));
StepDescription.displayName = "StepDescription";

const StepContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
));
StepContent.displayName = "StepContent";

const StepFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
));
StepFooter.displayName = "StepFooter";

const StepDivider = React.forwardRef<
  HTMLHRElement,
  React.HTMLAttributes<HTMLHRElement>
>(({ className, ...props }, ref) => (
  <hr ref={ref} className={cn("w-full border-[1px]", className)} {...props} />
));

StepDivider.displayName = "StepDivider";

export {
  Step,
  StepHeader,
  StepDivider,
  StepFooter,
  StepTitle,
  StepDescription,
  StepContent,
};
