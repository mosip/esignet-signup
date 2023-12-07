import { Buffer } from "buffer";

export const getSignInRedirectURL = (hash: string): string => {
  if (!!hash) {
    const signInQueryParams = Buffer.from(hash ?? "", "base64")?.toString();
    return process.env.REACT_APP_REDIRECT_SIGN_IN_URL + "?" + signInQueryParams;
  }

  return process.env.REACT_APP_REDIRECT_SIGN_UP_URL || "/";
};
