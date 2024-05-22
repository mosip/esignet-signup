import * as React from "react";

import { cn } from "~utils/cn";

import { Icons } from "./icons";

export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {}

const SearchBox = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, ...props }, ref) => {

    const clearInput = () => {
      // ref?.current = "";
    };

    return (
      <div className="relative h-full">
        <div
          className={cn(
            "h-full",
            "has-[input.border-destructive]:border-destructive",
            type === "password" && "flex rounded-md border-[1px] border-input"
          )}
        >
          <input
            type="text"
            className={cn(
              "flex h-12 w-full bg-transparent py-2 ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-[hsla(0,0%,51%)] focus-visible:outline-none disabled:cursor-not-allowed disabled:bg-muted-light-gray",
              "rounded-md border border-input px-3",
              className
            )}
            ref={ref}
            {...props}
          />
          {(
            <button
              id={props.id ? `${props.id}-clear-input` : undefined}
              type="button"
              className="flex h-full self-center px-3"
              onClick={clearInput}
            >
              <Icons.close className="h-5 w-5 text-gray-500" />
            </button>
          )}
        </div>
      </div>
    );
  }
);

SearchBox.displayName = "SearchBox";

export { SearchBox };
