export const getThreeLetterLocale = (
    i18nLang: string,
    langCodeMap?: Record<string, string>
): string => {
    const lookup: Record<string, string> = {};

    for (const [three, two] of Object.entries(langCodeMap ?? {})) {
        lookup[three] = three;
        lookup[two] = three;
    }

    return lookup[i18nLang] ?? i18nLang;
};