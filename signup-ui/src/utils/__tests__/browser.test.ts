import { compareVersions } from "~utils/browser";

describe("compareVersions", () => {
  it("should return true when current version is greater than minimum version", () => {
    const result = compareVersions("1.2.3", "1.2.2");
    expect(result).toBe(true);
  });

  it("should return false when current version is less than minimum version", () => {
    const result = compareVersions("1.2.2", "1.2.3");
    expect(result).toBe(false);
  });

  it("should return true when current version is equal to minimum version", () => {
    const result = compareVersions("1.2.3", "1.2.3");
    expect(result).toBe(true);
  });
});
