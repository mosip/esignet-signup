# Overview

Using **dynamic forms** instead of hardcoded forms during registration in **eSignet signup**.

The intention is to create an **independent UI library** to provide this feature which can be used in **signup-ui** registration form.

For more details on how to use the `json-form-builder` library, please refer to the [official documentation](https://github.com/mosip/mosip-sdk/blob/master/json-form-builder/README.md).

## Form JSON Specification

For reference, see the [MOSIP UI JSON specification](https://docs.mosip.io/1.2.0/id-lifecycle-management/identity-issuance/registration-client/develop/registration-client-ui-specifications#field-spec-json-template).

> **Note:** Only the **Field spec JSON template** section from the above link is applicable here.

### Supported Attributes

For **eSignet signup** forms, only the attributes listed below need to be supported.  
The schema is compatible with both two-letter (e.g., `en`) and three-letter (e.g., `eng`) language codes.

### Configuring Signup Registration Form

The signup registration form can be configured by specifying an endpoint URL. However, the property names differ across environments:

* **Mock**:
    * Property Name: `mosip.signup.mock.get-schema.endpoint` or `MOSIP_SIGNUP_MOCK_GET_SCHEMA_ENDPOINT`
    * Description: URL pointing to the raw JSON schema defining the signup UI spec.
    * The schema must include the fields, their types, validation rules, and multilingual labels used for signup registration.
    * Mock Identity system has an endpoint which will return ui spec for signup registration form 
    * Example:
     `http://mock-identity-system.mockid/v1/mock-identity-system/identity/ui-spec`
* **Mosipid**: 
    * Property Name: `mosip.signup.mosipid.get-ui-spec.endpoint` or `MOSIP_SIGNUP_MOSIPID_GET_UI_SPEC_ENDPOINT`
    * Description: URL pointing to the raw JSON schema defining the signup UI spec.
    * The schema must include the fields, their types, validation rules, and multilingual labels used for signup registration.
    * In mosipid environment, kernel's masterdata has a separate endpoint which will return ui spec for signup registration form
    * Example:
     `http://masterdata.kernel/v1/masterdata/uispec/esignet-signup/latest?identitySchemaVersion=0.1`


## 📄 Signup UI spec

```json
{
    "schema": [
        {
            "id": "phone",
            "controlType": "phone",
            "labelName": {
                "en": "Phone",
                "km": "ទូរស័ព្ទ"
            },
            "placeholder": {
                "eng": "Enter your username",
                "khm": "សូមបញ្ចូលឈ្មោះអ្នកប្រើប្រាស់"
            },
            "validators": [],
            "required": false,
            "disabled": true,
            "prefix": [
                "+91"
            ],
            "alignmentGroup": "groupA"
        },
        {
            "id": "fullName",
            "capsLockCheck": true,
            "controlType": "textbox",
            "type": "simpleType",
            "labelName": {
                "eng": "Full Name in Khmer",
                "khm": "គោត្តនាម-នាម"
            },
            "placeholder": {
                "en": "Enter Full Name in Khmer",
                "km": "បញ្ចូលគោត្តនាម-នាមជាភាសាខ្មែរ"
            },
            "validators": [
                {
                    "regex": "^[\\u1780-\\u17FF\\u19E0-\\u19FF\\u1A00-\\u1A9F\\u0020]{1,30}$",
                    "error": {
                        "eng": "Full Name has to be in Khmer only",
                        "khm": "គោត្តនាម-នាមត្រូវតែមានតែអក្សរខ្មែរ"
                    },
                    "langCode": "km"
                },
                {
                    "regex": "^[a-zA-Z][a-zA-Z ]{1,30}$",
                    "error": {
                        "eng": "Full Name has to be in English only",
                        "khm": "ឈ្មោះ​ពេញ​ត្រូវតែសរសេរជាភាសាអង់គ្លេសតែប៉ុណ្ណោះ"
                    },
                    "langCode": "en"
                }
            ],
            "info": {
                "en": "Maximum 30 characters allowed with no alphabets or special characters, except space.",
                "km": "ជាអតិបរមា 30 តួអក្សរត្រូវបានអនុញ្ញាត និងមិនគួរមានលេខ ឬតួអក្សរពិសេសណាមួយឡើយ លើកលែងតែដកឃ្លា។"
            },
            "alignmentGroup": "groupB",
            "required": true
        },
        {
            "id": "password",
            "capsLockCheck": true,
            "controlType": "password",
            "labelName": {
                "eng": "Password",
                "khm": "ពាក្យសម្ងាត់"
            },
            "placeholder": {
                "eng": "Enter your password",
                "khm": "សូមបញ្ចូលពាក្យសម្ងាត់"
            },
            "validators": [
                {
                    "regex": "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\x5F\\W])(?=.{8,20})[a-zA-Z0-9\\x5F\\W]{8,20}$",
                    "error": {
                        "eng": "Password does not meet the password policy. Click on \"i\" icon to know the password policy"
                    }
                }
            ],
            "info": {
                "eng": "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character.",
                "khm": "ពាក្យសម្ងាត់ត្រូវតែមានយ៉ាងហោចណាស់ ៨ តួអក្សរ មានតួអក្សរធំមួយ តួអក្សរតូចមួយ លេខមួយ និងតួអក្សរពិសេសមួយ។"
            },
            "alignmentGroup": "groupC",
            "required": true
        },
        {
            "id": "gender",
            "controlType": "dropdown",
            "labelName": {
                "eng": "Gender",
                "khm": "ភេទ"
            },
            "info": {
                "eng": "Please select a gender from the dropdown",
                "khm": "សូមជ្រើសរើសភេទពីបញ្ជីទម្លាក់ចុះ"
            },
            "alignmentGroup": "groupD",
            "required": true
        },
        {
            "id": "dateOfBirth",
            "controlType": "date",
            "labelName": {
                "eng": "Date of Birth",
                "khm": "ថ្ងៃខែឆ្នាំកំណើត"
            },
            "placeholder": {
                "eng": "Select your date of birth",
                "khm": "សូមជ្រើសរើសថ្ងៃខែឆ្នាំកំណើតរបស់អ្នក"
            },
            "info": {
                "eng": "Select you date of birth from the calendar",
                "khm": "សូមជ្រើសរើសថ្ងៃខែឆ្នាំកំណើតរបស់អ្នកពីប្រតិទិន"
            },
            "format": "yyyy/MM/dd",
            "alignmentGroup": "groupE",
            "required": true
        },
        {
            "id": "encodedPhoto",
            "controlType": "photo",
            "labelName": {
                "eng": "Capture Photo",
                "khm": "ថតរូប"
            },
            "placeholder": {
                "eng": "Click to capture photo",
                "khm": "ចុចដើម្បីថតរូប"
            },
            "info": {
                "eng": "Please click here to capture your photo using your device's camera.",
                "khm": "សូមចុចទីនេះដើម្បីថតរូបរបស់អ្នកដោយប្រើកាមេរ៉ារបស់ឧបករណ៍របស់អ្នក។"
            },
            "required": true,
            "alignmentGroup": "groupF"
        },
        {
            "id": "preferredLang",
            "controlType": "textbox",
            "labelName": {
                "eng": "Preferred Lang",
                "khm": "ភាសាដែលចូលចិត្ត"
            },
            "info": {
                "eng": "Preferred Lang",
                "khm": "ភាសាដែលចូលចិត្ត"
            },
            "required": false,
            "disabled": true
        },
        {
            "id": "consent",
            "controlType": "checkbox",
            "labelName": {
                "eng": "I agree to <b><a target='_blank' href='https://www.example.com/'>Terms & Conditions</a></b> and <b><a href='https://www.example.com/'>Privacy Policy</a></b>, to store & process my information as required.",
                "khm": "ខ្ញុំយល់ព្រមនឹង <b><a target='_blank' href='https://www.example.com/'>លក្ខខណ្ឌ និង ការកំណត់</a></b> និង <b><a href='https://www.example.com/'>គោលការណ៍ឯកជនភាព</a></b> ដើម្បីរក្សាទុក និងដំណើរការព័ត៌មានរបស់ខ្ញុំតាមដែលត្រូវការ។"
            },
            "required": true,
            "alignmentGroup": "groupD"
        }
    ],
    "allowedValues": {
        "preferredLang": "khm",
        "gender": {
            "male": {
                "eng": "Male",
                "khm": "បុរស"
            },
            "female": {
                "eng": "Female",
                "khm": "ស្ត្រី"
            }
        }
    },
    "i18nValues": {
        "errors": {
            "required": {
                "en": "This field is required",
                "ara": "هذه الخانة مطلوبه",
                "km": "វាលនេះត្រូវការទទួលបាន"
            },
            "passwordMismatch": {
                "en": "Passwords is not matching please check your password",
                "km": "ពាក្យសម្ងាត់មិនត្រូវគ្នាទេ សូមពិនិត្យពាក្យសម្ងាត់របស់អ្នក"
            },
            "capsLock": {
                "en": "Caps Lock is on",
                "km": "Caps Lock កំពុងបើក"
            }
        },
        "labels": {
            "password_confirm": {
                "en": "Confirm Password",
                "km": "បញ្ជាក់លេខសម្ងាត់"
            },
            "capturePhoto": {
                "en": "Capture Photo",
                "km": "ថតរូប"
            },
            "clickToUpload": {
                "en": "Click to upload",
                "km": "ចុចដើម្បីបញ្ចូលឬថតរូប"
            }
        },
        "placeholders": {
            "password_confirm": {
                "eng": "Enter your password again",
                "khm": "បញ្ចូលលេខសម្ងាត់របស់អ្នកម្ដងទៀត"
            }
        }
    },
    "errors": {
        "required": {
            "en": "This field is required",
            "ara": "هذه الخانة مطلوبه",
            "km": "វាលនេះត្រូវការទទួលបាន"
        },
        "passwordMismatch": {
            "en": "Passwords is not matching please check your password",
            "km": "ពាក្យសម្ងាត់មិនត្រូវគ្នាទេ សូមពិនិត្យពាក្យសម្ងាត់របស់អ្នក"
        },
        "capsLock": {
            "en": "Caps Lock is on",
            "km": "Caps Lock កំពុងបើក"
        }
    },
    "language": {
        "mandatory": [
            "khm"
        ],
        "optional": [
            "eng"
        ],
        "langCodeMap": {
            "khm": "km",
            "eng": "en"
        }
    },
    "maxUploadFileSize": 5242880
}
```
