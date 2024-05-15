/**
 * Compares two semantic versioning strings to determine if the current version is greater than or equal to the minimum version.
 *
 * @param {string} currentVersion  The current version string to compare, in the format 'major.minor.patch'.
 * @param {string} minVersion  The minimum version string to compare against, in the same format.
 * @returns {boolean}  Returns true if `currentVersion` is greater than or equal to `minVersion`, otherwise false.
 */
export const compareVersions = (currentVersion: string, minVersion: string) => {
  const currentParts = currentVersion.split(".").map(Number);
  const minParts = minVersion.split(".").map(Number);

  for (let i = 0; i < minParts.length; i++) {
    if (currentParts[i] > minParts[i]) return true;
    if (currentParts[i] < minParts[i]) return false;
  }
  return true;
};
