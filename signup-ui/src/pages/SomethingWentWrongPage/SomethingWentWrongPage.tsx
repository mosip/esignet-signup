import { getReasonPhrase } from "http-status-codes";
import { useLocation } from "react-router-dom";

import { ReactComponent as SomethingWentWrongSvg } from "~assets/svg/something-went-wrong.svg";
import ErrorPageTemplate from "~templates/ErrorPageTemplate";

export const SomethingWentWrongPage = () => {
  const {
    state: { code },
  } = useLocation();

  return (
    <ErrorPageTemplate
      title={getReasonPhrase(code)}
      description="Our experts are working hard to make things working again. Please try again later."
      image={<SomethingWentWrongSvg />}
    />
  );
};
