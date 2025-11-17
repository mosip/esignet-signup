package io.mosip.testrig.apirig.signup.utils;

import io.mosip.testrig.apirig.utils.WebSocketClientUtil;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

import org.apache.log4j.Logger;

public class SignupCustomWebSocketClientUtil extends WebSocketClientUtil {
	
	private static final Logger logger = Logger.getLogger(SignupCustomWebSocketClientUtil.class);

    public SignupCustomWebSocketClientUtil(String cookie, String subscribeDestination, String sendDestination) {
        super(cookie, subscribeDestination, sendDestination);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        // Your custom close logic
    	logger.info("Custom close reason: " + closeReason);

    }
}