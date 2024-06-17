import { checkBrowserCompatible } from "../checkBrowserCompatible";

enum Browser {
  Chrome = "chrome",
  Firefox = "firefox",
  Edge = "edge",
  Safari = "safari",
}

type BrowserCompatType = {
  [K in Browser]?: string;
};

const DEFAULT_MIN_BROWSER_COMPAT: BrowserCompatType = {
  [Browser.Chrome]: "118.0.5993.72",
  [Browser.Firefox]: "118.0.2",
  [Browser.Edge]: "118.0.2088.46",
  [Browser.Safari]: "14.1",
};

describe("checkBrowserCompatible", () => {
  const originalUserAgent = window.navigator.userAgent;

  describe("Chrome", () => {
    beforeEach(() => {
      jest
        .spyOn(navigator, "userAgent", "get")
        .mockReturnValue(
          "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
        );
    });

    it("should return true when browser is compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
        chrome: "118.0.5993.72",
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(true);
    });

    it("should return false when browser is not compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
        [Browser.Chrome]: "128.0.5993.72",
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(false);
    });
  });

  describe("Safari", () => {
    beforeEach(() => {
      jest
        .spyOn(navigator, "userAgent", "get")
        .mockReturnValue(
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Safari/605.1.15"
        );
    });

    it("should return true when browser is compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(true);
    });

    it("should return false when browser is not compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
        [Browser.Safari]: "18.2",
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(false);
    });
  });

  describe("Edge", () => {
    beforeEach(() => {
      jest
        .spyOn(navigator, "userAgent", "get")
        .mockReturnValue(
          "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0"
        );
    });

    it("should return true when browser is compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(true);
    });

    it("should return false when browser is not compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
        [Browser.Edge]: "130.0.1111.11",
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(false);
    });
  });

  describe("Firefox", () => {
    beforeEach(() => {
      jest
        .spyOn(navigator, "userAgent", "get")
        .mockReturnValue(
          "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:126.0) Gecko/20100101 Firefox/126.0"
        );
    });

    it("should return true when browser is compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(true);
    });

    it("should return false when browser is not compatible", () => {
      const minBrowserCompatibility: BrowserCompatType = {
        ...DEFAULT_MIN_BROWSER_COMPAT,
        [Browser.Firefox]: "130.9.9",
      };

      const result = checkBrowserCompatible(minBrowserCompatibility);
      expect(result).toBe(false);
    });
  });
});
