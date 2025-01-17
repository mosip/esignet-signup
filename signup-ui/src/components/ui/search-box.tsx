import * as React from "react";

import { cn } from "~utils/cn";

import { Icons } from "./icons";

export interface SearchBoxProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  searchRef?: React.RefObject<HTMLInputElement>;
}

const SearchBox = React.forwardRef<HTMLInputElement, SearchBoxProps>(
  ({ className, type, searchRef, ...props }) => {
    const clearInput = () => {
      if (searchRef && "current" in searchRef && searchRef.current) {
        searchRef.current.value = "";
        props.onChange &&
          props.onChange({
            target: { value: "" },
          } as React.ChangeEvent<HTMLInputElement>);
      }
    };

    return (
      <div className="relative h-full">
        <div
          className={cn(
            "h-full w-full",
            "has-[input.border-destructive]:border-destructive",
            "relative inline-flex items-center"
          )}
        >
          <input
            type="search"
            className={cn(
              "flex h-12 w-full bg-transparent py-2 ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-[hsla(0,0%,51%)] focus-visible:outline-none disabled:cursor-not-allowed disabled:bg-muted-light-gray",
              "rounded-md border border-[#CCCCCC] pl-10 pr-6",
              className
            )}
            ref={searchRef}
            {...props}
          />
          <button
            id={props.id ? `${props.id}-search-icon` : undefined}
            type="button"
            className="absolute block px-3"
          >
            <Icons.searchIcon />
          </button>
          {searchRef?.current?.value && (
            <button
              id={props.id ? `${props.id}-clear-input` : undefined}
              type="button"
              className="absolute block cursor-pointer right-[12px] ps-2"
              onClick={clearInput}
            >
              <Icons.closeIcon />
            </button>
          )}
        </div>
      </div>
    );
  }
);

SearchBox.displayName = "SearchBox";

export { SearchBox };
