import { StringMap } from "../constants/types";

interface ILangConfig {
  languages_2Letters: StringMap;
  rtlLanguages: string[];
  langCodeMapping: StringMap;
}

export default ILangConfig;