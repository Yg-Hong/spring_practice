package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFSocketType;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Component;

@Component
public class UdsDatagramClient {

    private final int BUFFER_SIZE = 2048;

    public String sendPacket(String sendMsg, int timeoutSec) {
        File sendSockFile = new File("/tmp/.webc_c_0");
        File recvSockFile = new File("/tmp/.webc_server");

        if (!recvSockFile.exists()) {
            return null;
        }

        if (sendSockFile.exists()) {
            sendSockFile.delete();
        }

        try (AFUNIXDatagramSocket socket = AFUNIXDatagramSocket.newInstance(AFSocketType.SOCK_DGRAM)) {
            socket.setSoTimeout(timeoutSec * 1000);
            socket.bind(AFUNIXSocketAddress.of(sendSockFile));

            byte[] sendBytes = sendMsg.getBytes();
            AFSocketAddress serverAddress = AFUNIXSocketAddress.of(recvSockFile);

            // DatagramPacket을 생성하고 전송
            DatagramPacket packet = new DatagramPacket(sendBytes, sendBytes.length, serverAddress);
            socket.send(packet);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);

            return new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (IOException e) {
            return null;
        }
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
