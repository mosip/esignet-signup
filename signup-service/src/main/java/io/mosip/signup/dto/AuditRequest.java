/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.util.AuditEventType;
import io.mosip.signup.util.AuditEvent;
import lombok.Data;

@Data
public class AuditRequest {

    private AuditEventType eventType;
    private AuditEvent eventId;
    private AuditEvent eventName;
    private String actionTimeStamp;
    private String hostName;
    private String hostIp;
    private String applicationId;
    private String applicationName;
    private String createdBy;
    private String sessionUserId;
    private String sessionUserName;
    private String id;
    private String idType;
    private String moduleName;
    private String moduleId;
    private String description;

    public AuditRequest(AuditEvent auditEvent, AuditEventType auditEventType, String applicationName, String sessionUserId,
                        String id, String moduleName, String description){

        this.eventId = auditEvent;
        this.eventName = auditEvent;
        this.eventType = auditEventType;
        this.actionTimeStamp = IdentityProviderUtil.getUTCDateTime();
        this.hostName = "localhost";
        this.hostIp = "localhost";
        this.sessionUserId = sessionUserId;
        this.sessionUserName = sessionUserId;
        this.id = id;
        this.idType = "transaction";
        this.moduleName = moduleName;
        this.moduleId = moduleName;
        this.description = description;
        this.applicationId = applicationName;
        this.applicationName = applicationName;
        this.createdBy = applicationName;
    }
}
