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
        String sessionId = "0"; // 실제로는 세션 기반 ID를 받아야 함.
        File clientSockFile = new File(CLIENT_SOCK_FILE + sessionId);
        File serverSockFile = new File(SERVER_SOCK_FILE);

        // 기존 소켓 파일 삭제
        if (clientSockFile.exists()) {
            clientSockFile.delete();
        }

        // 서버 소켓 파일이 존재하는지 확인
        if (!serverSockFile.exists()) {
            return null; // 서버가 실행 중이지 않음
        }

        try (AFUNIXDatagramSocket socket = AFUNIXDatagramSocket.newInstance()) {
            // 클라이언트 소켓 바인딩
            socket.bind(AFUNIXSocketAddress.of(clientSockFile));
            // 타임아웃 설정
            socket.setSoTimeout(timeout * 1000);

            // 서버로 메시지 전송
            byte[] sendData = sendMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                AFUNIXSocketAddress.of(serverSockFile));
            socket.send(sendPacket);

            // 서버 응답 수신
            byte[] recvData = new byte[BUFFER_SIZE];
            DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
            socket.receive(recvPacket);

            // 응답을 문자열로 변환하여 반환
            return new String(recvPacket.getData(), 0, recvPacket.getLength());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
