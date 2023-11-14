import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import Backend from "i18next-http-backend";
import ICU from "i18next-icu";
import { initReactI18next } from "react-i18next";

import { locales } from "./locales/index";

i18n
  // follow ICU format
  .use(ICU)
  // detect user language
  .use(LanguageDetector)
  // detect available locale files
  .use(Backend)
  // pass the i18n instance to react-i18next.
  .use(initReactI18next)
  // init i18next
  .init({
    resources: locales,
    fallbackLng: (window as any)._env_.DEFAULT_LANG, //default language
    debug: false,
    interpolation: {
      escapeValue: false, // not needed for react as it escapes by default
    },
  });

export default i18n;
