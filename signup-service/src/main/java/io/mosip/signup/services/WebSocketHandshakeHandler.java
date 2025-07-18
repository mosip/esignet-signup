/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import io.mosip.signup.dto.IdentityVerificationTransaction;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.util.AuditEvent;
import io.mosip.signup.util.AuditEventType;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.mosip.signup.util.SignUpConstants.VALUE_SEPARATOR;


@Slf4j
@Component
public class WebSocketHandshakeHandler extends DefaultHandshakeHandler {

    @Autowired
    CacheUtilService cacheUtilService;

    @Autowired
    AuditHelper auditHelper;

    private static final String SLOTID_QUERY_PARAM = "slotId=";
    private static final String SLOT_COOKIE_NAME = SignUpConstants.IDV_SLOT_ALLOTTED+"=";

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        log.debug("Started to determine user aka slotId with headers : {}", request.getHeaders());
        HttpHeaders headers = request.getHeaders();

        Optional<String> transactionCookie = Optional.empty();
        for(String cookie : headers.getOrEmpty(HttpHeaders.COOKIE)) {
            String[] result = cookie.split(";");
            transactionCookie = Arrays.stream(result).filter( v -> v.trim().startsWith(SLOT_COOKIE_NAME)).findFirst();
            if(transactionCookie.isPresent()) {
                break;
            }
        }

        if(transactionCookie.isEmpty())
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);

        String[] cookieParts = transactionCookie.get().split(SLOT_COOKIE_NAME);
        if (cookieParts.length < 2) {
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);
        }
        String cookieValue = cookieParts[1].trim();
        log.debug("HandshakeHandler cookie: {}", cookieValue);
        String transactionId = cookieValue.split(VALUE_SEPARATOR)[0].trim();
        IdentityVerificationTransaction transaction = cacheUtilService.getSlotAllottedTransaction(transactionId);

        String queryParam = request.getURI().getQuery();
        log.debug("HandshakeHandler queryParam >>> {} with transaction: {}", queryParam, transactionId);
        if(queryParam == null || queryParam.split(SLOTID_QUERY_PARAM).length <= 1 || transaction == null ||
                cookieValue.split(VALUE_SEPARATOR).length <= 1 ||
                !transaction.getSlotId().equals(queryParam.split(SLOTID_QUERY_PARAM)[1]) ||
                !transaction.getSlotId().equals(cookieValue.split(VALUE_SEPARATOR)[1])) {
            log.error("SlotId in the handshake url doesn't match the slotId in the transaction");
            auditHelper.sendAuditTransaction(AuditEvent.HANDSHAKE_FAILED, AuditEventType.ERROR, transactionId,null);
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);
        }

        final String username = transactionId.concat(VALUE_SEPARATOR).concat(transaction.getSlotId());
        cacheUtilService.setVerifiedSlotTransaction(transactionId, transaction.getSlotId(), transaction);
        auditHelper.sendAuditTransaction(AuditEvent.HANDSHAKE_SUCCESS, AuditEventType.SUCCESS, transactionId,null);

        return new Principal() {
            @Override
            public String getName() {
                return username;
            }
        };
    }
}
