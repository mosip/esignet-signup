import { Opaque } from "type-fest";

import { HttpError } from "~services/api.service";

export type ApiError = HttpError | Error;

export type JsonDate = Opaque<string, "JsonDate">;
