package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
//@RequiredArgsConstructor
public class UdsClient {

    public String sendPacket(String sendMsg, int timeoutSec) {
        // 소켓 파일 경로 지정
        Path socketPath = Path.of("/tmp/.webc_server");

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
