/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.config;

import io.mosip.signup.services.WebSocketHandshakeHandler;
import io.mosip.signup.util.WebSocketChannelInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketHandshakeHandler webSocketHandshakeHandler;

    @Autowired
    private WebSocketChannelInterceptor webSocketChannelInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/v1/signup/ws");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //By default, only same origin requests are allowed, should take the origin from properties
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .setHandshakeHandler(webSocketHandshakeHandler);
    }
}
