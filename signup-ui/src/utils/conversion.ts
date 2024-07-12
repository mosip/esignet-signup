import languageConfigService from "~services/langConfig.service"; // Import the getLocaleConfiguration function
import { KeyValueStringObject } from "~typings/types";

/**
 * It will convert the identity verifier detail data to i18n data
 * @param data KeyValueStringObject
 * @returns KeyValueStringObject
 */
export const convertToI18nData = async (
  data: KeyValueStringObject
): Promise<KeyValueStringObject> => {
  const langMappingData: any =
    await languageConfigService.getLocaleConfiguration();

  const transformedLangMap: any = {...langMappingData.langCodeMapping};

  const langCodeList = Object.keys(transformedLangMap);

  // setting all language code data to empty object
  for (const langCode in transformedLangMap) {
    transformedLangMap[langCode] = {};
  }

  // iterating identity verifier data
  // and adding they respective detail
  // to i18n data map
  for (const masterKey in data) {
    const masterValue = data[masterKey] as KeyValueStringObject;

    for (const key in masterValue) {
      const value = masterValue[key] as KeyValueStringObject;
      // checking if the key is language code
      // otherwise it will be a nested object
      if (langCodeList.includes(key)) {
        transformedLangMap[key][masterKey] = value;
      } else {
        for (const langCode in value) {
          if (!transformedLangMap[langCode].hasOwnProperty(masterKey)) {
            transformedLangMap[langCode][masterKey] = {};
          }
          transformedLangMap[langCode][masterKey][key] = value[langCode];
        }
      }
    }
  }

  // transforming the 3 letter lang
  // code to 2 letter lang code
  for (const key in transformedLangMap) {
    const langCode = langMappingData.langCodeMapping[key];
    transformedLangMap[langCode] = transformedLangMap[key];
    delete transformedLangMap[key];
  }
  return transformedLangMap;
};
