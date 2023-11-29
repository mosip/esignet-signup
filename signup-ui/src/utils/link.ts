export const getSignInRedirectURL = (hash: string): string => {
    if (!!hash) {
        return process.env.REACT_APP_REDIRECT_SIGN_IN_URL + hash;
    }

    return process.env.REACT_APP_REDIRECT_SIGN_UP_URL || "/";
}
