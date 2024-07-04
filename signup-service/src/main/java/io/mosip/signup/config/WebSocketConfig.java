package io.mosip.signup.config;

import io.mosip.signup.services.IdentityVerificationHandshakeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private IdentityVerificationHandshakeHandler identityVerificationHandshakeHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/v1/signup/ws");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //By default, only same origin requests are allowed, should take the origin from properties
        registry.addEndpoint("/ws").setAllowedOrigins("*").setHandshakeHandler(identityVerificationHandshakeHandler);
    }
}
