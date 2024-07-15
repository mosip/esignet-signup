import { KeyboardEvent } from "react";

export const handleInputFilter = (
  event: KeyboardEvent<HTMLInputElement>,
  condition: string
) => {
  const allowedKeys = ["Backspace", "Delete", "ArrowLeft", "ArrowRight", "a", "c", "v"];

  // Allow select all key: Ctrl + "a"
  // Allow copy key: Ctrl + "c"
  // Allow paste key: Ctrl + "v"
  if (
    (event.key === "a" || event.key === "c" || event.key === "v") &&
    !event.ctrlKey
  ) {
    event.preventDefault();
    return;
  }

  if (allowedKeys.includes(event.key)) {
    return; // Allow these keys
  }

  const allowedCharacters = new RegExp(condition);

  if (!allowedCharacters.test(event.key)) {
    event.preventDefault(); // Prevent non-allowed characters
  }

  // handling Unidentified key from Android keyboard by hard-filtering the Unidentified key
  if (event.key === "Unidentified") {
    event.currentTarget.value = event.currentTarget.value
    .split("")
    .filter(newKey => new RegExp(condition).test(newKey))
    .join("");
    event.preventDefault();
  }
};
