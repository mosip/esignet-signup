import { useTranslation } from "react-i18next";

import { Sizes } from "../constants/types";
import ILoadingIndicator from "../models/loadingIndicator.model";
import { ReactComponent as LoadingIcon } from "~assets/svg/loading-icon.svg";

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
}: ILoadingIndicator) => {
  const { t } = useTranslation("translation", { keyPrefix: i18nKeyPrefix });

  return (
    <>
      <div role="status" className="flex justify-center items-center">
        <LoadingIcon style={dynamicSize[size]} className="mr-2 rtl:ml-2 w-8 h-8 text-orange-500 animate-spin dark:text-gray-600 fill-secondary"/>
        <span className="sr-only">Loading...</span>
        {message && t(message, msgParam)}
      </div>
    </>
  );
};

export default LoadingIndicator;
