import { getSetting } from "./setting";
import { checkSlotHandlers } from "./slot-checking";

export const handlers = [...getSetting, ...checkSlotHandlers];
