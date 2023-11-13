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
    <div className="w-full flex h-[calc(100vh-14vh)] items-center justify-center p-16 px-32">
      <div className="w-full h-full bg-white rounded-[10px] flex flex-col items-center justify-center gap-y-12 shadow-[0_4px_10px_rgb(0,0,0,0.1)]">
        {image}
        <div className="flex flex-col items-center gap-y-2">
          <h1 className="text-2xl">{title}</h1>
          <p className="text-center text-gray-500">{description}</p>
        </div>
      </div>
    </div>
  );
};
