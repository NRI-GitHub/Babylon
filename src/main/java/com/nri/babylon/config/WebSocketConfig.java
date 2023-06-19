package com.nri.babylon.config;

import com.nri.babylon.controller.ProxyWebSocketHandler;
import org.kurento.tutorial.groupcall.CallHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private CallHandler callHandler;

    @Autowired
    private ProxyWebSocketHandler proxyWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(callHandler, "/groupcall");
        registry.addHandler(proxyWebSocketHandler, "/proxy");
    }
}
