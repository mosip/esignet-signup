import { useTranslation } from "react-i18next";

import { ReactComponent as TranslationIcon } from "~assets/svg/translation-icon.svg";
import { langLabel } from "~constants/i18n";

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
            placeholder={langLabel[i18n.language as keyof typeof langLabel]}
          />
        </SelectTrigger>
        <SelectContent className="w-12">
          <SelectItem value="en">English</SelectItem>
          <SelectItem value="km">ខ្មែរ</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
};
