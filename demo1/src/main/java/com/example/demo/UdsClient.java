package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UdsClient {

    private final UdsClientConfig config;
    private final File sendSockFile;
    private final File recvSockFile;

    public UdsClient(String sessionId) {
        this.sendSockFile = new File("/tmp/.webc_c_" + (sessionId.isEmpty() ? "0" : sessionId));
        this.recvSockFile = new File("/tmp/.webc_server");
    }

    public String sendPacket(String sendMsg, int timeoutSec) {
        if (!recvSockFile.exists()) {
            return null; // 리턴 조건: 서버 소켓 파일이 없으면 요청을 보낼 수 없음
        }

        Path socketPath = Path.of("/tmp/.webc_server");
        // 기존 소켓 파일이 존재하면 삭제
        if (sendSockFile.exists()) {
            sendSockFile.delete();
        }

        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            // 유닉스 도메인 소켓 주소 생성
            UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);
            serverSocket.bind(address);
            // 주소 정보 출력
            System.out.println("Unix Domain Socket Address: " + address.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "echo";
    }

}
