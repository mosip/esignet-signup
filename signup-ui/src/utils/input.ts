import { KeyboardEvent } from "react";

export const handleInputFilter = (
  event: KeyboardEvent<HTMLInputElement>,
  condition: string
) => {
  const allowedKeys = ["Backspace", "Delete", "ArrowLeft", "ArrowRight", "a"];

  // Allow select all key: Ctrl + "a"
  if (event.key === "a" && !event.ctrlKey) {
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
};
