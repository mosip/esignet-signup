import * as React from "react";

import { cn } from "~utils/cn";

import { Icons } from "./icons";

export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, ...props }, ref) => {
    const [showPassword, setShowPassword] = React.useState(false);

    const togglePasswordVisibility = () => {
      setShowPassword(!showPassword);
    };

    const inputType = type === "password" && showPassword ? "text" : type;

    return (
      <div className="relative h-full">
        <input
          type={inputType}
          className={cn(
            "flex h-12 w-full rounded-md border border-input bg-transparent px-3 py-2 ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-[hsla(0,0%,51%)] focus-visible:outline-none disabled:cursor-not-allowed disabled:bg-muted-light-gray",
            className
          )}
          ref={ref}
          {...props}
        />
        {type === "password" && (
          <button
            type="button"
            className="absolute right-3 top-1/2 -translate-y-1/2"
            onClick={togglePasswordVisibility}
          >
            {showPassword ? (
              <Icons.eyeOff className="h-5 w-5 text-gray-500" />
            ) : (
              <Icons.eye className="h-5 w-5 text-gray-500" />
            )}
          </button>
        )}
      </div>
    );
  }
);

Input.displayName = "Input";

export { Input };
