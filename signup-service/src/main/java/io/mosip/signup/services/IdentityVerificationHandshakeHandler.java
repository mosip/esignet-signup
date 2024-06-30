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
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Component
public class IdentityVerificationHandshakeHandler extends DefaultHandshakeHandler {

    @Autowired
    CacheUtilService cacheUtilService;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        log.info("Started to determine user aka slotId with headers : {}", request.getHeaders());
        HttpHeaders headers = request.getHeaders();

        String cookieName = SignUpConstants.IDV_SLOT_ALLOTTED+"=";
        Optional<String> transactionCookie = headers.getOrEmpty(HttpHeaders.COOKIE)
                .stream()
                .filter( cookie -> cookie.startsWith(cookieName))
                .findFirst();

        if(transactionCookie.isEmpty())
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);

        String transactionId = transactionCookie.get().substring(cookieName.length());
        IdentityVerificationTransaction transaction = cacheUtilService.getSlotAllottedTransaction(transactionId);

        List<String> values = headers.getOrEmpty("SlotId");
        if(values.isEmpty() || !values.get(0).equals(transaction.getSlotId())) {
            log.error("SlotId in the handshake header doesn't match the slotId in the transaction");
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);
        }

        return new Principal() {
            @Override
            public String getName() {
                return transactionId.concat("##").concat(transaction.getSlotId());
            }
        };
    }
}
