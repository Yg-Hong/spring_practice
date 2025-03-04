package com.example.demo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverviewUdsService {

    private final UdsClient udsClient;

    public Map<String, Object> getOverviewDaemonStatus() throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", "");
        int daemons = 0;

        String[] parsedResult = udsClient.sendMessage(UdsCmd.OverviewDaemonStatus);

        if (parsedResult.length == 0) {
            throw new IOException("temp");
        }

        if ("0".equals(parsedResult[1])) {

            int count = (parsedResult.length - 2) / 2;

            for (int n = 0; n < count; n++) {
                if (!"pv_xxx".equals(parsedResult[2 + (n * 2)])) {
                    Map<String, String> daemonData = new HashMap<>();
                    daemonData.put("name", parsedResult[2 + (n * 2)]);
                    daemonData.put("status", parsedResult[3 + (n * 2)]);

                    result.put(String.valueOf(n), daemonData);
                    daemons++;
                }
            }

            result.put("daemons", String.valueOf(daemons));
        } else {
            result.put("success", "1");
            result.put("log", "x Failed to get daemon status");
        }

        return result;
    }

    public Map<String, Object> getOverviewHomeInfo() throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", "");
        int daemons = 0;

        String[] parsedResult = udsClient.sendMessage(UdsCmd.OverviewHomeInfo);

        if (parsedResult.length == 0) {
            throw new IOException("temp");
        }

        try {
            result.put("models", parsedResult[2]);
            result.put("name", parsedResult[3]);
            result.put("id", parsedResult[4]);
            result.put("lanint", parsedResult[5]);
            result.put("lanip", parsedResult[6]);
            result.put("lansub", parsedResult[7]);
            result.put("wanint", parsedResult[8]);
            result.put("wanip", parsedResult[9]);
            result.put("wansub", parsedResult[10]);
            result.put("gate", parsedResult[11]);
            result.put("version", parsedResult[12]);

            String date = executeCommand("date");
            result.put("date", (date.isEmpty()) ? "-" : date);

        } catch (Exception e) {
            result.put("success", "1");
            result.put("log", "x Failed to get daemon status");
        }

        if ("0".equals(parsedResult[1])) {

            int count = (parsedResult.length - 2) / 2;

            for (int n = 0; n < count; n++) {
                if (!"pv_xxx".equals(parsedResult[2 + (n * 2)])) {
                    Map<String, String> daemonData = new HashMap<>();
                    daemonData.put("name", parsedResult[2 + (n * 2)]);
                    daemonData.put("status", parsedResult[3 + (n * 2)]);

                    result.put(String.valueOf(n), daemonData);
                    daemons++;
                }
            }

            result.put("daemons", String.valueOf(daemons));
        } else {
            result.put("success", "1");
            result.put("log", "x Failed to get daemon status");
        }

        return result;
    }

    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }
}

