import { StringMap } from "~constants/types";

export const getLocale = (currentLang: string, langCodeMapping: StringMap) => {
  return (
    Object.keys(langCodeMapping).find(
      (key) =>
        langCodeMapping[key as keyof typeof langCodeMapping] === currentLang
    ) ?? Object.keys(langCodeMapping)[0]
  );
};
