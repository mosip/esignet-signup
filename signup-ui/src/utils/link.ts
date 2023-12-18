import { Buffer } from "buffer";

export const getSignInRedirectURL = (redirectUrl: string | undefined, hash: string, defaultPath: string): string => {
  if (!!hash) {
    const signInQueryParams = Buffer.from(hash ?? "", "base64")?.toString();
    return redirectUrl + "?" + signInQueryParams;
  }

  return defaultPath;
};
