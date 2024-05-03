import { PageLayout } from "~layouts/PageLayout";

interface ErrorPageTemplateProps {
  title: string;
  description: string;
  image: React.ReactNode;
}

export const ErrorPageTemplate = ({
  title,
  description,
  image,
}: ErrorPageTemplateProps) => {
  return (
    <PageLayout
      className="h-[calc(100vh-13vh)] w-full items-center justify-center p-16 px-32 sm:px-[30px]"
      childClassName="h-full"
    >
      <div className="flex h-full w-full flex-col items-center justify-center gap-y-12 rounded-xl bg-white shadow-lg sm:shadow-none">
        {image}
        <div className="flex flex-col items-center gap-y-2">
          <h1 className="text-center text-2xl">{title}</h1>
          <p className="text-center text-gray-500">{description}</p>
        </div>
      </div>
    </PageLayout>
  );
};
