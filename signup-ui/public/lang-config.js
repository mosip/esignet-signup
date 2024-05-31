// Get the encoded string from the URL
const hashCode = window.location.hash.substring(1);

// Decode the string
const decodedBase64 = atob(hashCode);

// converting the decoded string to URLSearchParams
var urlSearchParams = new URLSearchParams(decodedBase64);

// Check if the URLSearchParams has the key 'ui_locales'
// If it has, set the value of 'ui_locales' to the DEFAULT_LANG
if (urlSearchParams.has('ui_locales')) {
  window._env_.DEFAULT_LANG = urlSearchParams.get('ui_locales').split('-')[0];
}
