import React from "react";
import ReactDOM from "react-dom/client";

import App from "./App";

import "./services/i18n.service";
import "react-tooltip/dist/react-tooltip.css";

if (process.env.NODE_ENV === "test") {
  import("./mocks/browser").then(({ worker }) => worker.start());
}

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
