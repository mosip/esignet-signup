import axios, { AxiosError } from "axios";
import { NavigateFunction } from "react-router-dom";

import { SOMETHING_WENT_WRONG } from "~constants/routes";

export const API_BASE_URL = process.env.REACT_APP_BASE_URL ?? "/api/";

export class HttpError extends Error {
  code: number;
  constructor(message: string, code: number) {
    super(message);
    this.code = code;
  }
}

// Create own axios instance with defaults.
export const ApiService = axios.create({
  withCredentials: true,
  baseURL: API_BASE_URL,
});

export const setupResponseInterceptor = (navigate: NavigateFunction) => {
  ApiService.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
      if (
        error.response?.status &&
        [400, 403, 404, 405, 415, 500, 502, 503, 504].includes(
          error.response.status
        )
      ) {
        navigate(SOMETHING_WENT_WRONG, {
          state: { code: error.response.status },
        });
      } else {
        return Promise.reject(error);
      }
    }
  );
};
