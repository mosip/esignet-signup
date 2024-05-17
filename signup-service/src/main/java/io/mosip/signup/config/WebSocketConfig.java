package io.mosip.signup.config;

import io.mosip.signup.services.IdentityVerificationHandshakeHandler;
import io.mosip.signup.services.IdentityVerificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private IdentityVerificationWebSocketHandler identityVerificationWebSocketHandler;

    @Autowired
    private IdentityVerificationHandshakeHandler identityVerificationHandshakeHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(identityVerificationWebSocketHandler,"/identity-verification")
                .setHandshakeHandler(identityVerificationHandshakeHandler);
        //By default, only same origin requests are allowed
    }
}
