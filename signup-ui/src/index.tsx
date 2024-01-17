import React from "react";
import ReactDOM from "react-dom/client";

import App from "./App";

import "./i18n/i18n";
import "react-tooltip/dist/react-tooltip.css";

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
