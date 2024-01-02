import { Buffer } from "buffer";

export const base64FullName = (fullName: string, language: string): string => {
    const fullNameObject = {
        "fullName": [
            {
                "language": language,
                "value": fullName
            }
        ] 
    };

const buffer = Buffer.from(JSON.stringify(fullNameObject));
return buffer.toString('base64');
}