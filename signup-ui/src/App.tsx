import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { Inspector, InspectParams } from "react-dev-inspector";

import "./App.css";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { BrowserRouter } from "react-router-dom";

import { langFontMapping } from "~constants/language";
import { HttpError } from "~services/api.service";

import { AppRouter } from "./app/AppRouter";

import NavBar from "~components/ui/nav-bar";
import Footer from "~components/ui/footer";
// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: Infinity,
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

  const { i18n } = useTranslation();

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
