package com.example.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UdsClient {

    private final String SOCKET_PATH;
    private final int BUFFER_SIZE;
    private final ReentrantLock lock = new ReentrantLock();

    private AFUNIXSocket socket;
    private OutputStream out;
    private InputStream in;

    public UdsClient() {
        this.SOCKET_PATH = "/tmp/.webc_server";
        this.BUFFER_SIZE = 4096;
    }

    @PostConstruct
    public void connect() throws IOException {
        lock.lock();
        try {
            if (isValid()) {
                return;
            }

            File socketFile = new File(SOCKET_PATH);

            // 소켓 파일이 존재하지 않으면 생성
            if (!socketFile.exists()) {
                log.warn("Socket file {} does not exist. Creating it...", SOCKET_PATH);
                createSocketFile(socketFile);
            }

            socket = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(getSocketAddress(SOCKET_PATH)));
            out = socket.getOutputStream();
            in = socket.getInputStream();
            log.info("Connected to {}", SOCKET_PATH);
        } finally {
            lock.unlock();
        }
    }

    @PreDestroy
    public void disconnect() throws IOException {
        lock.lock();
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log.info("Disconnected from server on {}", SOCKET_PATH);
            }
        } finally {
            lock.unlock();
        }
    }

    private void createSocketFile(File socketFile) throws IOException {
        try {
            boolean created = socketFile.createNewFile();
            if (!created) {
                throw new IOException(
                    "Failed to create socket file: " + socketFile.getAbsolutePath());
            }
            log.info("Socket file created: {}", socketFile.getAbsolutePath());

            socketFile.setReadable(true, true);
            socketFile.setWritable(true, false);
            socketFile.setExecutable(true, true);

            log.info("Socket file permissions set: rwx------");
        } catch (IOException e) {
            log.error("Error creating socket file", e);
            throw e;
        }
    }

    public boolean isValid() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void reconnect() throws IOException {
        lock.lock();
        try {
            if (this.isValid()) {
                return;
            }

            socket = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(getSocketAddress(SOCKET_PATH)));
            out = socket.getOutputStream();
            in = socket.getInputStream();
            log.info("Reconnected to UDS: {}", SOCKET_PATH);
        } finally {
            lock.unlock();
        }
    }

    public String[] sendMessage(UdsCmd udsCmd) throws IOException {
        lock.lock();
        try {
            reconnect();

            out.write(udsCmd.getCmd().getBytes());
            return sendToWebcDaemon();
        } finally {
            lock.unlock();
        }
    }

    public String[] sendMessage(UdsCmd udsCmd, String paramA) throws IOException {
        lock.lock();
        try {
            reconnect();

            String command = udsCmd.getCmd() + paramA;

            out.write(command.getBytes());
            return sendToWebcDaemon();
        } finally {
            lock.unlock();
        }
    }

    public String[] sendMessage(UdsCmd udsCmd, String paramA, String paramB) throws IOException {
        lock.lock();
        try {
            reconnect();

            String command = udsCmd.getCmd() + paramA + ":" + paramB;

            out.write(command.getBytes());
            return sendToWebcDaemon();
        } finally {
            lock.unlock();
        }
    }

    private String[] sendToWebcDaemon() throws IOException {
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
                log.info("InputStream closed.");
            }
            if (out != null) {
                out.close();
                log.info("OutputStream closed.");
            }
        } catch (IOException e) {
            log.error("Error while closing streams", e);
        }
    }
}