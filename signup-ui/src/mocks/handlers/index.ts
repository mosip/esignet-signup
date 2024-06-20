import { getSetting } from "./setting";
import { getSlot } from "./slot-checking";

export const handlers = [...getSetting, ...getSlot];
