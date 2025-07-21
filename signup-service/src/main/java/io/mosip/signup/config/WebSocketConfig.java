/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.config;

import io.mosip.signup.ws.WebSocketHandshakeHandler;
import io.mosip.signup.ws.WebSocketChannelInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${mosip.signup.ws.inbound.message.size.mb:3}")
    private int inboundMessageSizeInMB;

    @Value("${mosip.signup.ws.outbound.message.size.mb:1}")
    private int outboundMessageSizeInMB;

    @Value("${mosip.signup.ws.allowed.origin:*}")
    private String allowedOrigin;

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
                .setAllowedOrigins(allowedOrigin)
                .setHandshakeHandler(webSocketHandshakeHandler);
    }

    @Override
    public void configureWebSocketTransport(org.springframework.web.socket.config.annotation.WebSocketTransportRegistration registration) {
        // Set the maximum size for incoming text messages (decoded)
        // Default is often 64KB. You'll need to increase this significantly.
        // 1 MB = 1024 * 1024 bytes
        registration.setMessageSizeLimit(inboundMessageSizeInMB * 1024 * 1024);
        registration.setSendBufferSizeLimit(outboundMessageSizeInMB * 1024 * 1024);
        //registration.setSendTimeLimit(20 * 1000);
    }
}
