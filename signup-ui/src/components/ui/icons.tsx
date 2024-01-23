import * as React from "react";

import InfoPng from "~assets/png/info-38-2.png";
import { ReactComponent as ArrowLeft } from "~assets/svg/arrow-left.svg";
import { ReactComponent as Check } from "~assets/svg/check.svg";
import { ReactComponent as ChevronDown } from "~assets/svg/chevron-down.svg";
import { ReactComponent as ChevronUp } from "~assets/svg/chevron-up.svg";
import { ReactComponent as EyeOff } from "~assets/svg/eye-off.svg";
import { ReactComponent as Eye } from "~assets/svg/eye.svg";
import { ReactComponent as Loader } from "~assets/svg/loader.svg";
import { ReactComponent as X } from "~assets/svg/x.svg";

export const Icons = {
  back: ArrowLeft,
  check: Check,
  chevronDown: ChevronDown,
  chevronUp: ChevronUp,
  close: X,
  eye: Eye,
  eyeOff: EyeOff,
  info: React.forwardRef<
    HTMLImageElement,
    React.ImgHTMLAttributes<HTMLImageElement>
  >(({ alt, ...props }, ref) => (
    <img ref={ref} src={InfoPng} alt={alt} {...props} />
  )),
  loader: Loader,
  arrow: React.forwardRef<SVGSVGElement, React.SVGAttributes<SVGSVGElement>>(
    (props, ref) => (
      <svg
        width="24"
        height="12"
        viewBox="0 0 24 12"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        {...props}
        ref={ref}
      >
        <rect width="24" height="2" fill="white" />
        <path
          d="M24 1C18 1 18 11 12 11C6 11 6 0.999999 8.74228e-07 0.999999"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinejoin="round"
        />
      </svg>
    )
  ),
};
