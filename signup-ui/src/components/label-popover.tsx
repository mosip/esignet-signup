import {
  Popover,
  PopoverArrow,
  PopoverContent,
  PopoverTrigger,
} from "./ui/popover";

interface LabelPopoverProps {
  icon: React.ReactNode;
  children: React.ReactNode;
}

export const LabelPopover = ({ icon, children }: LabelPopoverProps) => {
  return (
    <>
      <div className="block sm:hidden">
        <Popover>
          <PopoverTrigger asChild>{icon}</PopoverTrigger>
          <PopoverContent side="right" className="text-xs">
            {children}
            <PopoverArrow className="fill-[#FFFFFF] stroke-[#BCBCBC]" />
          </PopoverContent>
        </Popover>
      </div>
      <div className="hidden sm:block">
        <Popover>
          <PopoverTrigger asChild>{icon}</PopoverTrigger>
          <PopoverContent side="bottom" className="text-xs">
            {children}
            <PopoverArrow className="fill-[#FFFFFF] stroke-[#BCBCBC]" />
          </PopoverContent>
        </Popover>
      </div>
    </>
  );
};
