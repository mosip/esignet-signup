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

  const start = cleanValue.slice(0, separatorIdx);
  const end = cleanValue.slice(-visibleEnd);

  const maskedLength = Math.max(
    cleanValue.length - (separatorIdx + visibleEnd),
    0
  );

  return start + "*".repeat(maskedLength) + end;
}