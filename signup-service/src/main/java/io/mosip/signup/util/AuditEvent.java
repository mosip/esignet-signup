/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.util;

public enum AuditEvent {

    GENERATE_CHALLENGE,
    VERIFY_CHALLENGE,
    REGISTER,
    REGISTER_STATUS_CHECK,
    RESET_PASSWORD,
    INITIATE_IDENTITY_VERIFICATION,
    IDENTITY_VERIFICATION_SLOT,
    IDENTITY_VERIFIER;
}
