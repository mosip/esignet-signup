// get the query params from the url
const queryString = window.location.search;

// converting the query param string to URLSearchParams
var urlSearchParams = new URLSearchParams(queryString);

// set fallback language to 'en' if DEFAULT_LANG is not set
// leave it if it is already set
if (!window._env_.FALLBACK_LANG) {
  window._env_.FALLBACK_LANG = window._env_.DEFAULT_LANG
  ? window._env_.DEFAULT_LANG
  : "en";
}

// Check if the URLSearchParams has the key 'ui_locales'
// If it has, set the value of 'ui_locales' to the DEFAULT_LANG
if (urlSearchParams.has("ui_locales") && urlSearchParams.get("ui_locales")) {
  window._env_.DEFAULT_LANG = urlSearchParams.get("ui_locales").split("-")[0];
} else if (!!localStorage.getItem("esignet-signup-language")) {
  window._env_.DEFAULT_LANG = localStorage.getItem("esignet-signup-language");
}
