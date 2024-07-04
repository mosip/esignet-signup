interface PageLayoutProps {
  children: React.ReactNode;
  className?: string;
  childClassName?: string;
  noBackground?: boolean;
}

export const PageLayout = ({
  children,
  childClassName,
  className,
  noBackground = false,
}: PageLayoutProps) => {
  return (
    <div
      className={`section-background relative flex ${
        className
          ? className
          : "flex-1 items-center justify-center"
      }`}
    >
      <img
        className={
          noBackground ? "hidden" : "top_left_bg_logo absolute left-1 top-1"
        }
        alt="top left background"
      />
      <img
        className={
          noBackground ? "hidden" : "top_right_bg_logo absolute right-1 top-1"
        }
        alt="top right background"
      />
      <div className={`z-10 w-full mx-4 ${childClassName}`}>{children}</div>
      <img
        className={
          noBackground
            ? "hidden"
            : "bottom_left_bg_logo absolute bottom-1 left-1"
        }
        alt="bottom left background"
      />
      <img
        className={
          noBackground
            ? "hidden"
            : "bottom_right_bg_logo absolute bottom-1 right-1"
        }
        alt="bottom right background"
      />
    </div>
  );
};
