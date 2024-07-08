package io.mosip.signup.services;

import io.mosip.signup.dto.IdentityVerificationTransaction;
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
import java.util.Map;
import java.util.Optional;

import static io.mosip.signup.util.SignUpConstants.SOCKET_USERNAME_SEPARATOR;


@Slf4j
@Component
public class IdentityVerificationHandshakeHandler extends DefaultHandshakeHandler {

    @Autowired
    CacheUtilService cacheUtilService;

    private static final String SLOTID_QUERY_PARAM = "slotId=";
    private static final String SLOT_COOKIE_NAME = SignUpConstants.IDV_SLOT_ALLOTTED+"=";

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        log.info("Started to determine user aka slotId with headers : {}", request.getHeaders());
        HttpHeaders headers = request.getHeaders();

        log.info("headers.getOrEmpty(HttpHeaders.COOKIE) ; {}", headers.getOrEmpty(HttpHeaders.COOKIE));

        String transactionCookie = "";
        for(String cookie : headers.getOrEmpty(HttpHeaders.COOKIE)) {
            log.info("cookie ; {}", cookie);
            if(cookie.startsWith(SLOT_COOKIE_NAME))
                transactionCookie = cookie;
        }

        if(transactionCookie.isEmpty())
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);

        String transactionId = transactionCookie.substring(SLOT_COOKIE_NAME.length());
        log.info("cookie  transactionId; {}", transactionId);
        IdentityVerificationTransaction transaction = cacheUtilService.getSlotAllottedTransaction(transactionId);

        String queryParam = request.getURI().getQuery();
        log.info("*** queryParam >>> {} with transaction: {}", queryParam, transaction);
        if(queryParam == null || queryParam.split(SLOTID_QUERY_PARAM).length <= 1 || transaction == null || 
                !transaction.getSlotId().equals(queryParam.split(SLOTID_QUERY_PARAM)[1])) {
            log.error("SlotId in the handshake url doesn't match the slotId in the transaction");
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);
        }

        final String username = transactionId.concat(SOCKET_USERNAME_SEPARATOR).concat(transaction.getSlotId());
        cacheUtilService.setVerifiedSlotTransaction(transactionId, transaction.getSlotId(), transaction);

        return new Principal() {
            @Override
            public String getName() {
                return username;
            }
        };
    }
}
