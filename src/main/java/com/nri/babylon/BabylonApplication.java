package com.nri.babylon;

import org.kurento.client.KurentoClient;
import org.kurento.tutorial.groupcall.CallHandler;
import org.kurento.tutorial.groupcall.RoomManager;
import org.kurento.tutorial.groupcall.UserRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@SpringBootApplication
public class BabylonApplication {

	public static void main(String[] args) {
		SpringApplication.run(BabylonApplication.class, args);
	}

}
