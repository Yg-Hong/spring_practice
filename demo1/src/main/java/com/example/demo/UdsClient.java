package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Component;

@Component
public class UdsClient {

    private final File sendSockFile;
    private final File recvSockFile;

    public UdsClient() {
        this.sendSockFile = new File("/tmp/.webc_c_0");
        this.recvSockFile = new File("/tmp/.webc_server");
    }

    public String sendPacket(String message, int timeoutSec) {
        if (!recvSockFile.exists()) {
            return null; // 리턴 조건: 서버 소켓 파일이 없으면 요청을 보낼 수 없음
        }

        // 기존 소켓 파일이 존재하면 삭제
        if (sendSockFile.exists()) {
            sendSockFile.delete();
        }

        try (AFUNIXDatagramSocket socket = AFUNIXDatagramSocket.newInstance()) {
            socket.setSoTimeout(timeoutSec * 1000);

            SocketAddress sendEndpoint = getSocketAddress("/tmp/.webc_c_0");
            AFUNIXSocketAddress sendAddress = AFUNIXSocketAddress.of(sendEndpoint);
            socket.bind(sendAddress);

            SocketAddress recvEndpoint = getSocketAddress("/tmp/.webc_server");
            AFUNIXSocketAddress recvAddress = AFUNIXSocketAddress.of(recvEndpoint);

            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(sendData, sendData.length, recvAddress));

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
