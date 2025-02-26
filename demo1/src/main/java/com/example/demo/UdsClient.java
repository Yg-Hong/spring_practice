package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Component;

@Component
public class UdsClient {

    private final File clientSockFile;
    private final File serverSockFile;

    public UdsClient() {
        this.clientSockFile = new File("/tmp/.webc_c_0");
        this.serverSockFile = new File("/tmp/.webc_server");
    }

    public String sendPacket(String message, int timeoutSec) {
        if (!serverSockFile.exists()) {
            return null; // 리턴 조건: 서버 소켓 파일이 없으면 요청을 보낼 수 없음
        }

        // 기존 소켓 파일이 존재하면 삭제
        if (clientSockFile.exists()) {
            clientSockFile.delete();
        }

        try (AFUNIXSocket socket = AFUNIXSocket.newInstance()) {
            SocketAddress serverAddress = AFUNIXSocketAddress.of(serverSockFile);
            socket.connect(serverAddress);

            // 송신
            OutputStream out = socket.getOutputStream();
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // 수신
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String response = reader.readLine();

            return response != null ? response : "No response";
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "echo";
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
