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
    <div className="h-screen flex flex-col items-center justify-center gap-y-12">
      {image}
      <div className="flex flex-col items-center gap-y-2">
        <h1 className="text-2xl">{title}</h1>
        <p className="text-center text-gray-500">{description}</p>
      </div>
    </div>
  );
};
