import { http, HttpResponse } from "msw";

// endpoint to be intercepted
const testConnectionEP = "http://localhost:8088/v1/signup/test";

const testConnectionSuccess = http.get(testConnectionEP, async () => {
  return HttpResponse.json({
    responseTime: "2024-03-25T18:10:18.520Z",
    response: {
      connection: true,
    },
    errors: [],
  });
});

export const testConnectionHandlers = [testConnectionSuccess];
