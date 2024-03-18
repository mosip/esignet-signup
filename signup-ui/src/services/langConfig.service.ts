import axios from "axios";

import { StringMap } from "../constants/types";
import ILangConfig from "../models/langConfig.model";

const defaultConfigEndpoint = "/locales/default.json";

/**
 * fetchs and return the locale configuration stored in public folder
 * @returns json object
 */
const getLocaleConfiguration = async (): Promise<ILangConfig> => {
  const endpoint = process.env.PUBLIC_URL + defaultConfigEndpoint;

  const response = await axios.get(endpoint);
  return response.data;
};

const getLangCodeMapping = async (): Promise<StringMap> => {
  let localConfig: ILangConfig = await getLocaleConfiguration();
  let reverseMap = Object.entries(localConfig.langCodeMapping).reduce(
    (pv, [key, value]) => ((pv[value] = key), pv),
    {} as StringMap
  );
  return reverseMap;
};

const langConfigService = {
  getLocaleConfiguration,
  getLangCodeMapping,
};

export default langConfigService;
