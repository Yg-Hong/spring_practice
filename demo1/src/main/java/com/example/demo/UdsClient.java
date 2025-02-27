package com.example.demo;

import jakarta.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
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
    public void connect() throws IOException {
        if (isValid()) {
            return;
        }
        log.info("Connecting to UDS: {}", SOCKET_PATH);

        socket = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(getSocketAddress(SOCKET_PATH)));
        out = new BufferedOutputStream(socket.getOutputStream());
        in = new BufferedInputStream(socket.getInputStream());

        log.info("UDS Connected Completely to {}", SOCKET_PATH);
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

    public void reconnect() throws IOException {
        if (this.isValid()) {
            return;
        }

        SocketAddress endpoint = getSocketAddress(SOCKET_PATH);
        socket = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(endpoint));
        out = socket.getOutputStream();
        in = socket.getInputStream();
    }

    public String[] sendMessage(String message) throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            log.info("UDS NOT CONNECTED, reconnecting...");
            reconnect();
        }

        out.write(message.getBytes());
        out.flush();

        byte[] buffer = new byte[BUFFER_SIZE];
        int numRead = in.read(buffer);

        String result = null;
        if (numRead > 0) {
            result = new String(buffer, 0, numRead);
        }
        cleanUp();

        return result == null ? new String[0] : parsePacket(result);
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

    private void cleanUp() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            log.error("Error while closing streams", e);
        }
    }
}
