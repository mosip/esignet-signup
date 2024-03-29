import {
  Popover,
  PopoverArrow,
  PopoverContent,
  PopoverTrigger,
} from "./ui/popover";

interface IconLabelPopoverProps {
  icon: React.ReactNode;
  children: React.ReactNode;
}

export const IconLabelPopover = ({ icon, children }: IconLabelPopoverProps) => {
  return (
    <>
      <div className="flex items-center sm:hidden">
        <Popover>
          <PopoverTrigger>{icon}</PopoverTrigger>
          <PopoverContent side="right" className="text-xs">
            {children}
            <PopoverArrow className="fill-[#FFFFFF] stroke-[#BCBCBC]" />
          </PopoverContent>
        </Popover>
      </div>
      <div className="hidden items-center sm:flex">
        <Popover>
          <PopoverTrigger>{icon}</PopoverTrigger>
          <PopoverContent side="bottom" className="text-xs">
            {children}
            <PopoverArrow className="fill-[#FFFFFF] stroke-[#BCBCBC]" />
          </PopoverContent>
        </Popover>
      </div>
    </>
  );
};
