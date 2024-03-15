import { isEqual } from "lodash";
import { create } from "zustand";
import { devtools } from "zustand/middleware";

import { StringMap } from "~constants/types";

export type LanguageStore = {
  languages_2Letters: StringMap;
  setLanguages2Letters: (languages_2Letters: StringMap) => void;
  rtlLanguages: string[];
  setRtlLanguages: (rtlLanguages: string[]) => void;
  langCodeMapping: StringMap;
  setLangCodeMapping: (langCodeMapping: StringMap) => void;
  langFontMapping: StringMap;
  setLangFontMapping: (langFontMapping: StringMap) => void;
};

export const useLanguageStore = create<LanguageStore>()(
  devtools((set, get) => ({
    languages_2Letters: {
      km: "ខ្មែរ",
      en: "English",
    },
    setLanguages2Letters: (languages_2Letters: StringMap) => {
      const current = get();
      if (isEqual(current.languages_2Letters, languages_2Letters)) return;
      set((state) => ({ languages_2Letters }));
    },
    rtlLanguages: [],
    setRtlLanguages: (rtlLanguages: string[]) => {
      const current = get();
      if (isEqual(current.rtlLanguages, rtlLanguages)) return;
      set((state) => ({ rtlLanguages }));
    },
    langCodeMapping: {
      khm: "km",
      eng: "en",
    },
    setLangCodeMapping: (langCodeMapping: StringMap) => {
      const current = get();
      if (isEqual(current.langCodeMapping, langCodeMapping)) return;
      set((state) => ({ langCodeMapping }));
    },
    langFontMapping: {
      en: "font-inter",
      km: "font-kantumruypro",
    },
    setLangFontMapping: (langFontMapping) => {
      const current = get();
      if (isEqual(current.langFontMapping, langFontMapping)) return;
      set((state) => ({ langFontMapping }));
    },
  }))
);

export const languages2LettersSelector = (
  state: LanguageStore
): LanguageStore["languages_2Letters"] => state.languages_2Letters;

export const setLanguages2LettersSelector = (
  state: LanguageStore
): LanguageStore["setLanguages2Letters"] => state.setLanguages2Letters;

export const rtlLanguagesSelector = (
  state: LanguageStore
): LanguageStore["rtlLanguages"] => state.rtlLanguages;

export const setRtlLanguagesSelector = (
  state: LanguageStore
): LanguageStore["setRtlLanguages"] => state.setRtlLanguages;

export const langCodeMappingSelector = (
  state: LanguageStore
): LanguageStore["langCodeMapping"] => state.langCodeMapping;

export const setLangCodeMappingSelector = (
  state: LanguageStore
): LanguageStore["setLangCodeMapping"] => state.setLangCodeMapping;

export const langFontMappingSelector = (
  state: LanguageStore
): LanguageStore["langFontMapping"] => state.langFontMapping;

export const setLangFontMappingSelector = (
  state: LanguageStore
): LanguageStore["setLangFontMapping"] => state.setLangFontMapping;
