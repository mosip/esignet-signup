import { checkSlotHandlers } from "./slot-checking";
import { testConnectionHandlers } from "./test-connection";

export const handlers = [
  // intercept the "test connection" endpoint
  ...testConnectionHandlers,
  // intercept the "check slot" endpoint
  ...checkSlotHandlers,
];
