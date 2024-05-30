import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { Inspector, InspectParams } from "react-dev-inspector";

import "./App.css";

import { useCallback, useEffect } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { BrowserRouter } from "react-router-dom";

import { HttpError } from "~services/api.service";
import langConfigService from "~services/langConfig.service";

import { AppRouter } from "./app/AppRouter";
import {
  langFontMappingSelector,
  setLangCodeMappingSelector,
  setLangFontMappingSelector,
  setLanguages2LettersSelector,
  setRtlLanguagesSelector,
  useLanguageStore,
} from "./useLanguageStore";

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60 * 1000, // set to one minutes
      retry: (failureCount, error) => {
        // Do not retry on 4xx error codes
        if (error instanceof HttpError && String(error.code).startsWith("4")) {
          return false;
        }
        return failureCount !== 3;
      },
    },
  },
});

function App() {
  const isDev = process.env.NODE_ENV === "development";

  const {
    langFontMapping,
    setLanguages2Letters,
    setRtlLanguages,
    setLangCodeMapping,
    setLangFontMapping,
  } = useLanguageStore(
    useCallback(
      (state) => ({
        langFontMapping: langFontMappingSelector(state),
        setLanguages2Letters: setLanguages2LettersSelector(state),
        setRtlLanguages: setRtlLanguagesSelector(state),
        setLangCodeMapping: setLangCodeMappingSelector(state),
        setLangFontMapping: setLangFontMappingSelector(state),
      }),
      []
    )
  );

  useEffect(() => {
    try {
      langConfigService.getLocaleConfiguration().then((response: any) => {
        let lookup: { [key: string]: number } = {};
        let supportedLanguages = response.languages_2Letters;
        let langData = [];
        for (let lang in supportedLanguages) {
          //check to avoid duplication language labels
          if (!(supportedLanguages[lang] in lookup)) {
            lookup[supportedLanguages[lang]] = 1;
            langData.push({
              label: supportedLanguages[lang],
              value: lang,
            });
          }
        }
        setLangCodeMapping(response.langCodeMapping);
        setLanguages2Letters(response.languages_2Letters);
        setRtlLanguages(response.rtlLanguages);
        setLangFontMapping(response.langFontMapping);
      });
    } catch (error) {
      console.error("Failed to load rtl languages!");
    }
  }, []);

  const { i18n } = useTranslation();

  useEffect(() => {
    document.querySelector(":root")?.classList.add(langFontMapping[i18n.language])
  }, [langFontMapping, i18n.language])

  return (
    <div className={langFontMapping[i18n.language]}>
      {isDev && (
        <Inspector
          // props see docs:
          // https://github.com/zthxxx/react-dev-inspector#inspector-component-props
          keys={["control", "shift", "c"]}
          disableLaunchEditor={true}
          onClickElement={({ codeInfo }: InspectParams) => {
            if (!codeInfo?.absolutePath) return;
            const { absolutePath, lineNumber, columnNumber } = codeInfo;
            // you can change the url protocol if you are using in Web IDE
            window.open(
              `vscode://file/${absolutePath}:${lineNumber}:${columnNumber}`
            );
          }}
        />
      )}
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AppRouter />
        </BrowserRouter>
        <ReactQueryDevtools
          initialIsOpen={false}
          buttonPosition="bottom-left"
        />
      </QueryClientProvider>
    </div>
  );
}

export default App;
