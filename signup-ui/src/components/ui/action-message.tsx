import * as React from "react";

import { cn } from "~utils/cn";

export interface ActionMessageProps
  extends React.HTMLAttributes<HTMLDivElement> {}

const ActionMessage = React.forwardRef<HTMLDivElement, ActionMessageProps>(
  ({ className, hidden, children, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        "flex items-center justify-between bg-destructive/5 px-4 py-2",
        {
          hidden,
        },
        className
      )}
      {...props}
    >
      {children}
    </div>
  )
);

ActionMessage.displayName = "ActionMessage";

export { ActionMessage };
