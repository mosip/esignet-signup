import * as DropdownMenu from "@radix-ui/react-dropdown-menu";
import { useTranslation } from "react-i18next";

import { ReactComponent as TranslationIcon } from "~assets/svg/translation-icon.svg";
import { langFontMapping } from "~constants/language";

import locales from "../../public/locales/default.json";
import { Icons } from "./ui/icons";
import { cn } from "~utils/cn";

export const Language = () => {
  const { i18n } = useTranslation();

  const handleLanguageChange = (language: string) => {
    i18n.changeLanguage(language);
    window.location.reload();
  };

  return (
    <div className="flex">
      <TranslationIcon className="w-9 h-9 mr-2" />
      <DropdownMenu.Root>
        <DropdownMenu.Trigger asChild>
          <span
            className="inline-flex items-center justify-center bg-white outline-none hover:cursor-pointer text-[14px]"
            aria-label="Customise options"
          >
            {
              locales.languages_2Letters[
                i18n.language as keyof typeof locales.languages_2Letters
              ]
            }
            <Icons.chevronDown className="h-4 w-4 ml-1" />
          </span>
        </DropdownMenu.Trigger>
        <DropdownMenu.Portal>
          <DropdownMenu.Content
            className="min-w-[220px] bg-white rounded-md shadow-[0px_10px_38px_-10px_rgba(0,0,0,_0.35),_0px_10px_20px_-15px_rgba(0,0,0,_0.2)] will-change-[opacity,transform] data-[side=top]:animate-slideDownAndFade data-[side=right]:animate-slideLeftAndFade data-[side=bottom]:animate-slideUpAndFade data-[side=left]:animate-slideRightAndFade px-3 py-2"
            sideOffset={5}
          >
            {Object.entries(locales.languages_2Letters).map(([key, value]) => (
              <DropdownMenu.Item
                key={key}
                className={
                  cn(
                    "group text-[14px] leading-none flex items-center relative select-none outline-none data-[disabled]:pointer-events-none hover:font-bold cursor-pointer py-2 first:border-b-[1px]",
                    langFontMapping[key],
                    {
                      "font-bold": i18n.language === key
                    },
                  )
                }
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
