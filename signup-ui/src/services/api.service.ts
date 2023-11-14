import axios, { AxiosError } from "axios";
import { StatusCodes } from "http-status-codes";
import { NavigateFunction } from "react-router-dom";

import { LOCAL_STORAGE_EVENT } from "~constants/localStorage";
import { SOMETHING_WENT_WRONG } from "~constants/routes";
import { ApiError } from "~typings/core";

export const API_BASE_URL = process.env.REACT_APP_BASE_URL ?? "/api/";

export class HttpError extends Error {
  code: number;
  constructor(message: string, code: number) {
    super(message);
    this.code = code;
  }
}

/**
 * Converts possible AxiosError objects to normal Error objects
 *
 * @returns HttpError if AxiosError, else original error
 */
export const transformAxiosError = (error: Error): ApiError => {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      const statusCode = error.response.status;
      if (statusCode === StatusCodes.TOO_MANY_REQUESTS) {
        return new HttpError("Please try again later.", statusCode);
      }
      if (typeof error.response.data === "string") {
        return new HttpError(error.response.data, statusCode);
      }
      if (error.response.data?.message) {
        return new HttpError(error.response.data.message, statusCode);
      }
      if (error.response.statusText) {
        return new HttpError(error.response.statusText, statusCode);
      }
      return new HttpError(`Error: ${statusCode}`, statusCode);
    } else if (error.request) {
      return new Error(
        `There was a problem with your internet connection. Please check your network and try again. ${error.message}`
      );
    }
  }
  return error;
};

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
