import * as DropdownMenu from "@radix-ui/react-dropdown-menu";
import { useTranslation } from "react-i18next";

import { ReactComponent as TranslationIcon } from "~assets/svg/translation-icon.svg";
import { langFontMapping, languages_2Letters } from "~constants/language";
import { cn } from "~utils/cn";

import { Icons } from "./ui/icons";

export const Language = () => {
  const { i18n } = useTranslation();

  const handleLanguageChange = (language: string) => {
    i18n.changeLanguage(language);
    window.location.reload();
  };

  return (
    <div className="flex">
      <TranslationIcon className="mr-2 h-9 w-9" />
      <DropdownMenu.Root>
        <DropdownMenu.Trigger asChild>
          <span
            className="inline-flex items-center justify-center bg-white text-[14px] outline-none hover:cursor-pointer"
            aria-label="Customise options"
          >
            {
              languages_2Letters[
                i18n.language as keyof typeof languages_2Letters
              ]
            }
            <Icons.chevronDown className="ml-1 h-4 w-4" />
          </span>
        </DropdownMenu.Trigger>
        <DropdownMenu.Portal>
          <DropdownMenu.Content
            className="data-[side=top]:animate-slideDownAndFade data-[side=right]:animate-slideLeftAndFade data-[side=bottom]:animate-slideUpAndFade data-[side=left]:animate-slideRightAndFade z-50 min-w-[220px] rounded-md bg-white px-3 py-2 shadow-[0px_10px_38px_-10px_rgba(0,0,0,_0.35),_0px_10px_20px_-15px_rgba(0,0,0,_0.2)] will-change-[opacity,transform]"
            sideOffset={5}
          >
            {Object.entries(languages_2Letters).map(([key, value]) => (
              <DropdownMenu.Item
                key={key}
                className={cn(
                  "group relative flex cursor-pointer select-none items-center py-2 text-[14px] leading-none outline-none first:border-b-[1px] hover:font-bold data-[disabled]:pointer-events-none",
                  langFontMapping[key],
                  {
                    "font-bold": i18n.language === key,
                  }
                )}
                onSelect={() => handleLanguageChange(key)}
              >
                {value}
                <div className="ml-auto">
                  {i18n.language === key && (
                    <Icons.check className="h-4 w-4 text-orange-500" />
                  )}
                </div>
              </DropdownMenu.Item>
            ))}
            <DropdownMenu.Arrow className="fill-white" />
          </DropdownMenu.Content>
        </DropdownMenu.Portal>
      </DropdownMenu.Root>
    </div>
  );
};
