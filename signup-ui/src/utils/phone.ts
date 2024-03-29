export function maskPhoneNumber(phone: string, separatorIdx: number) {
  return phone.replace(
    new RegExp(`^.{${separatorIdx}}`, "g"),
    `${"x".repeat(separatorIdx)} `
  );
}
