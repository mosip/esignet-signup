// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom";

import { mswServer } from "./mocks/msw-server";

require("whatwg-fetch");

const matchers = require("jest-extended");
expect.extend(matchers);

jest.mock("react-i18next", () => ({
  ...jest.requireActual("react-i18next"),
  useTranslation: () => {
    const enFile = jest.requireActual("../public/locales/en.json");
    return {
      t: (stringKey: string) =>
        stringKey.split(".").reduce((result, key) => result[key], enFile),
      i18n: {
        language: "en",
        addResourceBundle: () => jest.fn(),
        changeLanguage: () => new Promise(() => {}),
      },
    };
  },
}));

beforeAll(() => mswServer.listen());
afterEach(() => mswServer.resetHandlers());
afterAll(() => mswServer.close());
