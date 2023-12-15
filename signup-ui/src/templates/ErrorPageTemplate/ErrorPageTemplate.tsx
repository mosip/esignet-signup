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
    <div className="flex h-[calc(100vh-14vh)] w-full items-center justify-center p-16 px-32 sm:px-[30px]">
      <div className="h-full bg-white flex w-full flex-col items-center justify-center gap-y-12 rounded-xl shadow-lg sm:shadow-none">
        {image}
        <div className="flex flex-col items-center gap-y-2">
          <h1 className="text-center text-2xl">{title}</h1>
          <p className="text-center text-gray-500">{description}</p>
        </div>
      </div>
    </div>
  );
};
