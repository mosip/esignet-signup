import { langCodeMapping } from "~constants/language";

export const getLocale = (currentLang: string) => {
  return (
    Object.keys(langCodeMapping).find(
      (key) =>
        langCodeMapping[key as keyof typeof langCodeMapping] === currentLang
    ) ?? Object.keys(langCodeMapping)[0]
  );
};
