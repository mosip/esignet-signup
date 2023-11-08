import { ReactComponent as PageUnderConstructionSvg } from "~assets/svg/page-under-construction.svg";
import ErrorPageTemplate from "~templates/ErrorPageTemplate";

export const UnderConstructionPage = () => {
  return (
    <ErrorPageTemplate
      title="Page Under Construction!"
      description="Our experts are working hard to make this page available. Meanwhile, we request you to please visit after some time."
      image={<PageUnderConstructionSvg />}
    />
  );
};
