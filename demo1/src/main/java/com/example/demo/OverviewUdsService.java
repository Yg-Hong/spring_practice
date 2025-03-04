package com.example.demo;

import java.io.IOException;
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

}

