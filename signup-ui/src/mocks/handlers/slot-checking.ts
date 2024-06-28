import { delay, http, HttpResponse } from "msw";

const checkSlotAvailabilityEP =
  "http://localhost:8088/v1/signup/identity-verification/slot";

const checkSlotHandlerSuccess = http.post(checkSlotAvailabilityEP, async () => {
  await delay(3000);

  return HttpResponse.json({
    responseTime: "2024-03-25T18:10:18.520Z",
    response: {
      slotId: "12345",
    },
    errors: [],
  });
});

export const checkSlotHandlerUnavailable = http.post(
  checkSlotAvailabilityEP,
  async () => {
    return HttpResponse.json({
      responseTime: "2024-03-25T18:10:18.520Z",
      response: null,
      errors: [{ errorCode: "slot_unavailable" }],
    });
  }
);

export const checkSlotHandlers = [checkSlotHandlerSuccess];
