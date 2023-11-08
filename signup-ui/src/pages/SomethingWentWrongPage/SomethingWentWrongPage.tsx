import { ReactComponent as SomethingWentWrongSvg } from "~assets/svg/something-went-wrong.svg";
import ErrorPageTemplate from "~templates/ErrorPageTemplate";

export const SomethingWentWrongPage = () => {
  return (
    <ErrorPageTemplate
      title="Something went wrong!"
      description="Our experts are working hard to make things working again."
      image={<SomethingWentWrongSvg />}
    />
  );
};
