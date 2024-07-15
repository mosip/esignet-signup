import { useTranslation } from "react-i18next";

import { ReactComponent as LoadingIcon } from "~assets/svg/loading-icon.svg";
import { cn } from "~utils/cn";

import { Sizes } from "../constants/types";
import ILoadingIndicator from "../models/loadingIndicator.model";

const dynamicSize: Sizes = {
  small: {
    width: "1.5rem",
    height: "1.5rem",
  },
  medium: {
    width: "2.5rem",
    height: "2.5rem",
  },
  large: {
    width: "5rem",
    height: "5rem",
  },
};

const LoadingIndicator = ({
  message = "",
  size = "medium",
  msgParam = "",
  i18nKeyPrefix = "loadingMsgs",
  iconClass = "",
  divClass = "",
}: ILoadingIndicator) => {
  const { t } = useTranslation("translation", { keyPrefix: i18nKeyPrefix });

  return (
    <>
      <div
        role="status"
        className={cn("flex items-center justify-center", divClass)}
      >
        <LoadingIcon
          style={dynamicSize[size]}
          className={cn(
            "mr-2 h-8 w-8 animate-spin fill-secondary text-primary rtl:ml-2 dark:text-gray-600",
            iconClass
          )}
        />
        <span className="sr-only">Loading...</span>
        {message && t(message, msgParam)}
      </div>
    </>
  );
};

export default LoadingIndicator;
