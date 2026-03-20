export function maskData(value: string, separatorIdx: number) {
  const cleanValue = value.replace(/\s+/g, "");

  // EMAIL MASKING
  if (cleanValue.includes("@")) {
    const [username, domain] = cleanValue.split("@");

    // If username length < 3 → mask completely
    if (username.length < 3) {
      return "*".repeat(username.length) + "@" + domain;
    }

    const start = username[0];
    const end = username[username.length - 1];
    const maskedLength = username.length - 2;

    return start + "*".repeat(maskedLength) + end + "@" + domain;
  }

  // GENERIC MASKING (phone / id / others)
  const visibleEnd = 2;

  const safeSeparatorIdx = Math.max(
    0,
    Math.min(separatorIdx, cleanValue.length)
  );
  const endVisible = Math.min(
    visibleEnd,
    Math.max(cleanValue.length - safeSeparatorIdx, 0)
  );

  const start = cleanValue.slice(0, safeSeparatorIdx);
  const end = cleanValue.slice(cleanValue.length - endVisible);

  const maskedLength = Math.max(
    cleanValue.length - (safeSeparatorIdx + endVisible),
    0
  );

  return start + "*".repeat(maskedLength) + end;
}
