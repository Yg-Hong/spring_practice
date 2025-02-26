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
    private AFUNIXSocket sock;
    private OutputStream out;
    private InputStream in;

    public UdsClient() {
        this.SOCKET_PATH = "/tmp/.webc_server";
        this.BUFFER_SIZE = 4096;
    }

    @PostConstruct
    public void connect() throws IOException {
        SocketAddress endpoint = getSocketAddress(SOCKET_PATH);
        sock = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(endpoint));
        out = sock.getOutputStream();
        in = sock.getInputStream();

        log.info("UDS Connected Completely to {}", endpoint);
    }

    @PreDestroy
    public void disconnect() throws IOException {
        if (sock != null && !sock.isClosed()) {
            sock.close();
            log.info("Disconnected from server on " + SOCKET_PATH);
        }
    }

    public boolean isValid() {
        if (sock == null) {
            return false;
        } else if (!sock.isConnected()) {
            return false;
        }

        return true;
    }

    public void reconnect() throws IOException {
        if (this.isValid()) {
            return;
        }

        SocketAddress endpoint = getSocketAddress(SOCKET_PATH);
        sock = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(endpoint));
        out = sock.getOutputStream();
        in = sock.getInputStream();
    }

    public String[] sendMessage(String message) throws IOException {
        if (sock == null || !sock.isConnected()) {
            log.info("UDS NOT CONNECTED!!!");
            throw new IOException("UDS Not Connected");
        }
        log.info("Now writing string({}) to the server...", message);
        out.write(message.getBytes());
        out.flush();

        byte[] buffer = new byte[BUFFER_SIZE];
        int numRead = in.read(buffer);
        if (numRead > 0) {
            return parsePacket(new String(buffer, 0, numRead));
        } else {
            return null;
        }
    }

    private String[] parsePacket(String result) {
        return result.split(":");
    }

//    public String sendPacket(String message, int timeoutSec) {
//        if (!serverSockFile.exists()) {
//            return null; // 리턴 조건: 서버 소켓 파일이 없으면 요청을 보낼 수 없음
//        }
//
//        try (AFUNIXSocket socket = AFUNIXSocket.newInstance()) {
//            SocketAddress serverAddress = AFUNIXSocketAddress.of(serverSockFile);
//            socket.connect(serverAddress);
//
//            // 송신
//            OutputStream out = socket.getOutputStream();
//            out.write(message.getBytes(StandardCharsets.UTF_8));
//            out.flush();
//
//            // 수신
//            InputStream inputStream = socket.getInputStream();
//            BufferedReader reader = new BufferedReader(
//                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//            String response = reader.readLine();
//
//            System.out.printf("response : %s\n", response);
//
//            return response != null ? response : "No response";
//        } catch (SocketException e) {
//            e.printStackTrace();
//            return null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return "echo";
//    }

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
