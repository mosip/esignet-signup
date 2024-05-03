interface PageLayoutProps {
  children: React.ReactNode;
}

export const PageLayout = ({ children }: PageLayoutProps) => {
  return (
    <div className="relative flex flex-1 items-center justify-center sm:flex-none">
      <img
        className="absolute left-1 top-1 block sm:hidden"
        src="/images/top.png"
        alt="top left background"
      />
      <div className="z-10 w-full">{children}</div>
      <img
        className="absolute bottom-1 right-1 block sm:hidden"
        src="/images/bottom.png"
        alt="bottom right background"
      />
    </div>
  );
};
