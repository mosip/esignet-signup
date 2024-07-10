import { useCallback, useEffect, useRef } from "react";
import * as DropdownMenu from "@radix-ui/react-dropdown-menu";
import { useTranslation } from "react-i18next";

import { ReactComponent as TranslationIcon } from "~assets/svg/translation-icon.svg";
import { cn } from "~utils/cn";
import { replaceUILocales } from "~utils/link";
import {
  langFontMappingSelector,
  languages2LettersSelector,
  useLanguageStore,
} from "~/useLanguageStore";

import { Icons } from "./ui/icons";

export const Language = () => {
  const { i18n } = useTranslation();
  const langRef = useRef(null);
  const { languages_2Letters, langFontMapping } = useLanguageStore(
    useCallback(
      (state) => ({
        languages_2Letters: languages2LettersSelector(state),
        langFontMapping: langFontMappingSelector(state),
      }),
      []
    )
  );

  const handleLanguageChange = (language: string) => {
    i18n.changeLanguage(language);

    const urlSearchParams = replaceUILocales(window.location.hash, language);
    // Encode the string
    const encodedBase64 = btoa(urlSearchParams.toString());
    const url =
      window.location.origin + window.location.pathname + "#" + encodedBase64;

    // Replace the current url with the modified url due to the language change
    window.history.replaceState(null, "", url);
  };

  // setting language dropdown value to current fallback language
  const setFallbackLng = (lng: string) => {
    const langToBeSet = languages_2Letters.hasOwnProperty(lng)
      ? lng
      : (window as any)._env_.FALLBACK_LANG;
    handleLanguageChange(langToBeSet);
  };

  useEffect(() => {
    // checking if language dropdown value is set or not
    // if not then use current i18n language to set it
    const refInterval = setInterval(() => {
      if (!langRef.current) {
        clearInterval(refInterval);
        setFallbackLng(i18n.language);
      }
    }, 1000);
  }, []);

  return (
    <div className="flex">
      <TranslationIcon className="mr-2 h-9 w-9" />
      <DropdownMenu.Root>
        <DropdownMenu.Trigger id="language-select-button" asChild>
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
            className={cn(
              "relative  z-50  rounded-md border border-[#BCBCBC] bg-white px-3 py-2 shadow-md outline-0 ",
              "data-[side=top]:animate-slideDownAndFade data-[side=right]:animate-slideLeftAndFade data-[side=bottom]:animate-slideUpAndFade data-[side=left]:animate-slideRightAndFade",
              "top-[-0.5rem] min-w-[220px] will-change-[opacity,transform]"
            )}
            sideOffset={5}
          >
            {Object.entries(languages_2Letters).map(([key, value]) => (
              <DropdownMenu.Item
                id={key + "_language"}
                key={key}
                ref={langRef}
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
                    <Icons.check className="h-4 w-4 text-primary" />
                  )}
                </div>
              </DropdownMenu.Item>
            ))}
            <DropdownMenu.Arrow asChild>
              <Icons.chevronUpSolid className="h-[7px] stroke-[#bcbcbc]" />
            </DropdownMenu.Arrow>
          </DropdownMenu.Content>
        </DropdownMenu.Portal>
      </DropdownMenu.Root>
    </div>
  );
};
