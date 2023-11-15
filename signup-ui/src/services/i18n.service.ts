import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import Backend from "i18next-http-backend";
import ICU from "i18next-icu";
import { initReactI18next } from "react-i18next";

i18n
  // follow ICU format
  .use(ICU)
  // detect available locale files
  .use(Backend)
  // detect user language
  .use(LanguageDetector)
  // pass the i18n instance to react-i18next.
  .use(initReactI18next)
  // init i18next
  .init({
    debug: false,
    fallbackLng: (window as any)._env_.DEFAULT_LANG, //default language
    interpolation: {
      escapeValue: false, // not needed for react as it escapes by default
    },
    backend: {
      loadPath: process.env.PUBLIC_URL + "/locales/{{lng}}.json",
    },
  });

export default i18n;
