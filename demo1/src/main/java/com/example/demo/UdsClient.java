package com.example.demo;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

@Component
public class UdsClient {

    private final UdsClientConfig config;

    public String sendPacket(String sendMsg) {
        String sessionId = "0";
        Path clientSockPath = Paths.get(config.getClientSocketPath());
        Path serverSockPath = Paths.get(config.getServerSocketPath());

        // 기존 클라이언트 소켓 파일이 있으면 삭제
        try {
            if (Files.exists(clientSockPath)) {
                Files.delete(clientSockPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete existing client socket file", e);
        }

        // DatagramChannel을 이용한 UDS Datagram 소켓 생성
        try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.UNIX)) {
            // 클라이언트 UDS 주소 바인딩
            UnixDomainSocketAddress clientAddr = UnixDomainSocketAddress.of(clientSockPath);
            channel.bind(clientAddr);

            // 서버 UDS 주소
            UnixDomainSocketAddress serverAddr = UnixDomainSocketAddress.of(serverSockPath);

            // 메시지 전송
            ByteBuffer buffer = ByteBuffer.allocate(config.getBufferSize());
            buffer.put(sendMsg.getBytes());
            buffer.flip();

            channel.send(buffer, serverAddr);

            // 응답 수신
            buffer.clear();
            channel.receive(buffer);
            buffer.flip();

            byte[] responseBytes = new byte[buffer.remaining()];
            buffer.get(responseBytes);
            return new String(responseBytes);

        } catch (IOException e) {
            throw new RuntimeException("Failed to communicate via UDS", e);
        }
    }

}
