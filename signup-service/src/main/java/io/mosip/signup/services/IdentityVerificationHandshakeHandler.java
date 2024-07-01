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

        Optional<String> transactionCookie = headers.getOrEmpty(HttpHeaders.COOKIE)
                .stream()
                .filter( cookie -> cookie.startsWith(SLOT_COOKIE_NAME))
                .findFirst();

        if(transactionCookie.isEmpty())
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);

        String transactionId = transactionCookie.get().substring(SLOT_COOKIE_NAME.length());
        IdentityVerificationTransaction transaction = cacheUtilService.getSlotAllottedTransaction(transactionId);

        String queryParam = request.getURI().getQuery();
        if(queryParam == null || queryParam.split(SLOTID_QUERY_PARAM).length <= 1 ||
                !transaction.getSlotId().equals(queryParam.split(SLOTID_QUERY_PARAM)[1])) {
            log.error("SlotId in the handshake url doesn't match the slotId in the transaction");
            throw new HandshakeFailureException(ErrorConstants.INVALID_TRANSACTION);
        }

        return new Principal() {
            @Override
            public String getName() {
                return transactionId.concat(SOCKET_USERNAME_SEPARATOR).concat(transaction.getSlotId());
            }
        };
    }
}
