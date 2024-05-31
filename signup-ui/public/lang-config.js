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

