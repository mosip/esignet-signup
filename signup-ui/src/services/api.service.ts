import axios, { AxiosError } from "axios";
import { NavigateFunction } from "react-router-dom";

import { SOMETHING_WENT_WRONG } from "~constants/routes";
import { getCsrfToken } from "~pages/shared/service";

const API_BASE_URL =
  process.env.NODE_ENV === "development"
    ? process.env.REACT_APP_API_BASE_URL
    : window.origin + "/v1/signup";

export const WS_BASE_URL =
  process.env.NODE_ENV === "development" || process.env.NODE_ENV === "test"
    ? `ws://${process.env.REACT_APP_API_BASE_URL?.split("://")[1]}`
    : `wss://${window.location.host}/v1/signup`;

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

export const AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

ApiService.interceptors.request.use(
  async (config) => {
    try {
      if (config.method?.toLowerCase() === "post") {
        let csrfToken = sessionStorage.getItem("csrfToken");
        if (!csrfToken) {
          csrfToken = await getCsrfToken();
          if (csrfToken) sessionStorage.setItem("csrfToken", csrfToken);
        }
        config.headers["X-XSRF-TOKEN"] = csrfToken;
      }
    } catch (error) {
      throw error;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

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
