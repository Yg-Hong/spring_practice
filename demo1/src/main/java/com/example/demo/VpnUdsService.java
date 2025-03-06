package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VpnUdsService {

    private final RedisTemplate<String, String> redisTemplate6379;
    private final RedisTemplate<String, String> redisTemplate6380;

    public VpnUdsService(
        @Qualifier("redisTemplate6379") RedisTemplate<String, String> redisTemplate6379,
        @Qualifier("redisTemplate6380") RedisTemplate<String, String> redisTemplate6380
    ) {
        this.redisTemplate6379 = redisTemplate6379;
        this.redisTemplate6380 = redisTemplate6380;
    }


    public Map<String, Object> getClientsByPage(int page, int rowNum) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", "");
        int cctvs = 0;
        int offset = (page - 1) * rowNum;

        ListOperations<String, String> listOps = redisTemplate6379.opsForList();
        HashOperations<String, String, String> hashOps = redisTemplate6379.opsForHash();
        ValueOperations<String, String> valueOps6380 = redisTemplate6380.opsForValue();

        Long total = listOps.size("client_list");
        if (total == null) {
            total = 0L;
        }
        long pages = (long) Math.ceil((double) total / rowNum);
        result.put("pages", String.valueOf(pages));
        result.put("total", String.valueOf(total));

        List<String> resArray = listOps.range("client_list", offset, offset + rowNum - 1);

        if (resArray != null) {
            for (String value : resArray) {
                if (value == null || value.isEmpty()) {
                    break;
                }

                Map<String, String> data = hashOps.entries("client:" + value);
                Map<String, String> cctvData = new HashMap<>();

                cctvData.put("index", String.valueOf(offset + cctvs + 1));
                cctvData.put("macAddr", data.getOrDefault("macAddr", "-"));
                cctvData.put("serialNumber", data.getOrDefault("serialNumber", "").trim());
                cctvData.put("publicip", data.getOrDefault("publicip", "-"));
                cctvData.put("vpnip", data.getOrDefault("vpnip", "-"));
                cctvData.put("tunnel_vpnip", data.getOrDefault("tunnel_vpnip", "-"));
                cctvData.put("tid", "1");
                cctvData.put("create_time", data.getOrDefault("create_time", "-"));
                cctvData.put("update_time", data.getOrDefault("update_time", "-"));
                cctvData.put("device", data.getOrDefault("device", "X"));

                Boolean exists = redisTemplate6379.hasKey("cctv:" + value);
                if (Boolean.TRUE.equals(exists)) {
                    String cctvStr = valueOps6380.get("cctv:" + value);
                    if (cctvStr != null) {
                        String[] cctvInfo = cctvStr.split(" ");
                        String pubkey = (cctvInfo.length > 2) ? cctvInfo[2] : "-";
                        cctvData.put("pubkey", pubkey);
                        cctvData.put("interface", (cctvInfo.length > 5) ? cctvInfo[5] : "-");

                        String trafficStr = valueOps6380.get("peer:" + pubkey);
                        String[] trafficInfo =
                            (trafficStr != null) ? trafficStr.split(",") : new String[]{};
                        String status = (trafficInfo.length >= 3) ? trafficInfo[2]
                            : "0"; // 0: red, 1: orange, 2: green
                        cctvData.put("status", status);
                    }
                } else {
                    cctvData.put("pubkey", "-");
                    cctvData.put("interface", "-");
                    cctvData.put("status", "0");
                }

                result.put(String.valueOf(cctvs), cctvData);
                cctvs++;
            }
        }

        result.put("cctvs", String.valueOf(cctvs));

        if (cctvs < 1) {
            result.put("success", "1");
            result.put("log", "x Failed to load client list");
        }

        return result;
    }

}
