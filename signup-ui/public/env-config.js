// Get the encoded string from the URL
const hashCode = window.location.hash.substring(1);

// Decode the string
const decodedBase64 = atob(hashCode);

var urlSearchParams = new URLSearchParams(decodedBase64);

// Convert the decoded string to JSON
var jsonObject = {};
urlSearchParams.forEach(function (value, key) {
  jsonObject[key] = value;
});

window._env_ = {
  // Set the default language, if ui_locales available from the URL else set it to khmer.
  DEFAULT_LANG: jsonObject.ui_locales ?? "km",
  DEFAULT_THEME: "",
  DEFAULT_FEVICON: "favicon.ico",
  DEFAULT_TITLE: "eSignet-Signup",
  SUPPORTED_LNG: ["en", "km"],
};
