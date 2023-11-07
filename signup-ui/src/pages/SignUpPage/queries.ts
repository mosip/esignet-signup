import { useQuery, UseQueryResult } from "react-query";

import { SettingsDto } from "~typings/types";

import { getSettings } from "./service";

export const SettingsKeys = {
  base: ["settings"] as const,
};

export const useSettings = (): UseQueryResult<SettingsDto, unknown> => {
  return useQuery<SettingsDto>(SettingsKeys.base, () => getSettings(), {
    staleTime: Infinity,
  });
};
