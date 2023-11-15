import { useTranslation } from "react-i18next";

import { ReactComponent as TranslationIcon } from "~assets/svg/translation-icon.svg";

import locales from "../../public/locales/default.json";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";

export const Language = () => {
  const { i18n } = useTranslation();

  const handleLanguageChange = (language: string) => {
    i18n.changeLanguage(language);
  };

  return (
    <div className="flex">
      <TranslationIcon className="w-full h-full" />
      <Select onValueChange={handleLanguageChange} defaultValue={i18n.language}>
        <SelectTrigger>
          <SelectValue
            placeholder={
              locales.languages_2Letters[
                i18n.language as keyof typeof locales.languages_2Letters
              ]
            }
          />
        </SelectTrigger>
        <SelectContent className="w-12">
          {Object.entries(locales.languages_2Letters).map(([key, value]) => (
            <SelectItem key={key} value={key}>
              {value}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
};
