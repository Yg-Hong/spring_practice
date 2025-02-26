package com.example.demo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class UdsClientConfig {

    @Value("${uds.client.socket.path:/tmp/.webc_client}")
    private String clientSocketPath;

    @Value("${uds.server.socket.path:/tmp/.webc_server}")
    private String serverSocketPath;

    @Value("${uds.socket.buffer.size:1024}")
    private int bufferSize;
}
