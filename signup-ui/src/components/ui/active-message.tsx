import * as React from "react";

import { cn } from "~utils/cn";

export interface ActiveMessageProps
  extends React.HTMLAttributes<HTMLDivElement> {}

const ActiveMessage = React.forwardRef<HTMLDivElement, ActiveMessageProps>(
  ({ className, hidden, children, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        "flex items-center justify-between bg-destructive/5 px-4 py-2",
        {
          hidden,
        }
      )}
      {...props}
    >
      {children}
    </div>
  )
);

ActiveMessage.displayName = "ActiveMessage";

export { ActiveMessage };
