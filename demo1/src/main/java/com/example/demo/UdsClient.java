package com.example.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UdsClient {

    @Value("${uds.socket.path:/tmp/.webc_server}")
    private String SOCKET_PATH;
    @Value("${uds.buffer.size}")
    private int BUFFER_SIZE;

    private AFUNIXSocket socket;
    private OutputStream out;
    private InputStream in;

    @PostConstruct
    public void init() {
        try {
            connect();
            log.info("UDS Connected Completely to {}", SOCKET_PATH);
        } catch (IOException e) {
            log.error("Failed to connect to UDS: {}", SOCKET_PATH, e);
        }
    }

    @PreDestroy
    public void cleanup() {
        disconnect();
    }

    public void connect() throws IOException {
        if (isValid()) {
            return;
        }

        socket = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(getSocketAddress(SOCKET_PATH)));
        out = new BufferedOutputStream(socket.getOutputStream());
        in = new BufferedInputStream(socket.getInputStream());
    }

    public synchronized void disconnect() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                log.info("Disconnected from UDS: {}", SOCKET_PATH);
            } catch (IOException e) {
                log.error("Error closing UDS connection", e);
            }
        }
    }

    public synchronized boolean isValid() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public String[] sendMessage(String message) throws IOException {
        if (!isValid()) {
            log.info("UDS NOT CONNECTED, reconnecting...");
            connect();
        }

        log.info("Sending message : {}", message);
        out.write(message.getBytes(StandardCharsets.UTF_8), 0, message.length() + 1);
        out.flush();

        byte[] buffer = new byte[BUFFER_SIZE];
        int numRead = in.read(buffer);

        if (numRead <= 0) {
            log.warn("No data received from UDS");
            return new String[0];
        }

        String response = new String(buffer, 0, numRead, StandardCharsets.UTF_8);
        log.debug("Received response: {}", response);

        return parsePacket(response);
    }

    private String[] parsePacket(String result) {
        return result.split(":");
    }

    private SocketAddress getSocketAddress(String socketName) throws IOException {
        if (socketName.startsWith("file:")) {
            // demo only: assume file: URLs are always handled by AFUNIXSocketAddress
            return AFUNIXSocketAddress.of(URI.create(socketName));
        } else if (socketName.contains(":/")) {
            // assume URI, e.g., unix:// or tipc://
            return AFSocketAddress.of(URI.create(socketName));
        }

        int colon = socketName.lastIndexOf(':');
        int slashOrBackslash = Math.max(socketName.lastIndexOf('/'), socketName.lastIndexOf('\\'));

        if (socketName.startsWith("@")) {
            // abstract namespace (Linux only!)
            return AFUNIXSocketAddress.inAbstractNamespace(socketName.substring(1));
        } else if (colon > 0 && slashOrBackslash < colon && !socketName.startsWith("/")) {
            // assume TCP socket
            String hostname = socketName.substring(0, colon);
            int port = Integer.parseInt(socketName.substring(colon + 1));
            return new InetSocketAddress(hostname, port);
        } else {
            // assume unix socket file name
            return AFUNIXSocketAddress.of(new File(socketName));
        }
    }
}
