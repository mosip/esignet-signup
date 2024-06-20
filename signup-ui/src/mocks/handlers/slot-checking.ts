import { delay, http, HttpResponse } from "msw";

import { GET_SLOT_ENDPOINT } from "../endpoints";

export const getSlot = [
  http.post(GET_SLOT_ENDPOINT, async () => {
    await delay(3000);

    return HttpResponse.json({
      responseTime: "2024-03-25T18:10:18.520Z",
      response: {
        slotId: "12345",
      },
      errors: [],
    });
  }),
];
