import { http, HttpResponse } from "msw";

import { UI_SETTING_ENDPOINT } from "../endpoints";

export const getSetting = [
  http.get(UI_SETTING_ENDPOINT, () => {
    return HttpResponse.json({
      responseTime: "2024-06-11T03:03:55.647Z",
      response: {
        configs: {
          "identifier.pattern": "^\\+855[1-9]\\d{7,8}$",
          "identifier.prefix": "+855",
          "captcha.site.key": "6LcRpoQpAAAAAASGTap2ZEfEhj4eTZwKUO9FL6Cj",
          "otp.length": 6,
          "password.pattern":
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\x5F\\W])(?=.{8,20})[a-zA-Z0-9\\x5F\\W]{8,20}$",
          "password.length.max": 20,
          "password.length.min": 8,
          "challenge.timeout": 60,
          "resend.attempts": 3,
          "resend.delay": 60,
          "fullname.pattern":
            "^[\\u1780-\\u17FF\\u19E0-\\u19FF\\u1A00-\\u1A9F\\u0020]{1,30}$",
          "status.request.delay": 20,
          "status.request.limit": 10,
          "popup.timeout": 10,
          "signin.redirect-url":
            "https://esignet.camdgc-dev1.mosip.net/authorize",
          "identifier.allowed.characters": "^[0-9]+",
          "identifier.length.min": 8,
          "identifier.length.max": 9,
          "fullname.allowed.characters":
            "^[\\u1780-\\u17FF\\u19E0-\\u19FF\\u1A00-\\u1A9F\\u0020]",
          "fullname.length.min": 1,
          "fullname.length.max": 30,
          "otp.blocked": 300,
          "send-challenge.captcha.required": false,
        },
      },
      errors: [],
    });
  }),
];
