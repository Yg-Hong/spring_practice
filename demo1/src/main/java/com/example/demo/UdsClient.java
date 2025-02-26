package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Component;

@Component
//@RequiredArgsConstructor
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

            AFUNIXSocketAddress sendAddress = new AFUNIXSocketAddress(sendSockFile);
            socket.bind(sendAddress);

        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
