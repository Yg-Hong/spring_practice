package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Service;

@Service
public class UdsClientService {

    private static final String CLIENT_SOCK_FILE = "/tmp/.webc_c_";
    private static final String SERVER_SOCK_FILE = "/tmp/.webc_server";
    private static final int BUFFER_SIZE = 2048;

    public String sendPacket(String sendMsg, int timeout) {
        String sessionId = "0";
        File clientSockFile = new File(CLIENT_SOCK_FILE + sessionId);
        File serverSockFile = new File(SERVER_SOCK_FILE);

        // 기존 소켓 파일 삭제
        if (clientSockFile.exists()) {
            clientSockFile.delete();
        }

        // 서버 소켓 파일 존재 확인
        if (!serverSockFile.exists()) {
            System.err.println("Error: Server socket file does not exist: " + SERVER_SOCK_FILE);
            return null;
        }

        // 서버 소켓 파일이 유효한 소켓인지 확인
        if (!serverSockFile.isFile()) {
            System.err.println(
                "Error: " + SERVER_SOCK_FILE + " is not a valid UNIX domain socket.");
            return null;
        }

        try (AFUNIXDatagramSocket socket = AFUNIXDatagramSocket.newInstance()) {
            // 클라이언트 소켓 바인딩
            socket.bind(AFUNIXSocketAddress.of(clientSockFile));
            socket.setSoTimeout(timeout * 1000);

            // 서버로 메시지 전송
            byte[] sendData = sendMsg.getBytes();
            AFUNIXSocketAddress serverAddress = AFUNIXSocketAddress.of(serverSockFile);

            System.out.println("Sending message to: " + serverAddress.getPath());

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                serverAddress);
            socket.send(sendPacket);

            // 서버 응답 수신
            byte[] recvData = new byte[BUFFER_SIZE];
            DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
            socket.receive(recvPacket);

            return new String(recvPacket.getData(), 0, recvPacket.getLength());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
