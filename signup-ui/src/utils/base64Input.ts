import { Buffer } from "buffer";

export const base64Input = (data: Record<string, any>): string => {
    const buffer = Buffer.from(JSON.stringify(data));
    return buffer.toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/\=+$/, '');
}
