import { useQuery, UseQueryResult } from "react-query";

import { RegisterStatusResponseDto, SettingsDto } from "~typings/types";

import { getRegisterStatus, getSettings } from "./service";

export const keys = {
  settings: ["settings"] as const,
  registerStatus: ["registerStatus"] as const,
};

export const useSettings = (): UseQueryResult<SettingsDto, unknown> => {
  return useQuery<SettingsDto>(keys.settings, () => getSettings(), {
    staleTime: Infinity,
  });
};

export const useRegisterStatus = (): UseQueryResult<
  RegisterStatusResponseDto,
  unknown
> => {
  return useQuery<RegisterStatusResponseDto>(
    keys.registerStatus,
    () => getRegisterStatus(),
    {
      staleTime: Infinity,
    }
  );
};
