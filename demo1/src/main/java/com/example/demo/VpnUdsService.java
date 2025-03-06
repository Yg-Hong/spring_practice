package com.example.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
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

    public Map<String, Object> getTunnels() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", "0");
        result.put("log", "");
        int n = 0;
        int lbRelayMode = 0;
        int ifnum = 0;

        try {
            ZSetOperations<String, String> zSetOps = redisTemplate6379.opsForZSet();
            ListOperations<String, String> listOps = redisTemplate6379.opsForList();
            HashOperations<String, String, String> hashOps = redisTemplate6379.opsForHash();
            ValueOperations<String, String> valueOps = redisTemplate6379.opsForValue();

            Long total = zSetOps.zCard("tclient");
            if (total == null) {
                total = 0L;
            }
            result.put("last", String.valueOf(total));
            result.put("total", String.valueOf(total));

            Set<String> tidSet = zSetOps.range("tclient", 0, 0);
            String tid = tidSet != null && !tidSet.isEmpty() ? tidSet.iterator().next() : "1";
            result.put("tid", tid);

            List<String> tunnelList = listOps.range("tid:" + tid, 0, -1);
            if (tunnelList == null || tunnelList.isEmpty()) {
                result.put("success", "1");
                result.put("log", "x Tunnel list is empty.");
                return result;
            }

            result.put("tunnels", String.valueOf(tunnelList.size()));

            String lbRelayModeStr = valueOps.get("lb_relay_mode");
            lbRelayMode = (lbRelayModeStr != null && !lbRelayModeStr.isEmpty()) ? Integer.parseInt(
                lbRelayModeStr) : 0;

            List<Map<String, String>> tunnels = new ArrayList<>();

            for (String vpnip : tunnelList) {
                Map<String, String> data = hashOps.entries("tunnel:" + vpnip);
                Map<String, String> tunnelData = new HashMap<>();

                tunnelData.put("vpnip", vpnip);
                tunnelData.put("vpnmask", data.getOrDefault("vpnmask", "-"));
                tunnelData.put("pubkey", data.getOrDefault("pubkey", "-"));
                tunnelData.put("allowed_ips", data.getOrDefault("allowed_ips", "-"));
                tunnelData.put("ctime", data.getOrDefault("ctime", "-"));
                tunnelData.put("utime", data.getOrDefault("utime", "-"));

                if (lbRelayMode > 0) {
                    ifnum = Integer.parseInt(data.getOrDefault("ifnum", "0"));
                    tunnelData.put("endpoint",
                        ifnum > lbRelayMode ? data.getOrDefault("endpoint2", "-")
                            : data.getOrDefault("endpoint", "-"));
                } else {
                    tunnelData.put("endpoint", data.getOrDefault("endpoint", "-"));
                }

                // IP Range Calculation
                String maxtunStr = data.get("maxtun");
                int maxIpNum = (maxtunStr != null) ? Integer.parseInt(maxtunStr) : 0;

                String vpnmask = data.get("vpnmask");
                if (vpnmask != null) {
                    long netaddr = ipToLong(vpnip) & ipToLong(vpnmask);
                    Double ipnumObj = zSetOps.score("ipalloc:" + tid, vpnip);
                    int ipnum = (ipnumObj != null) ? ipnumObj.intValue() : 0;
                    int used = ipnum;
                    int unused = maxIpNum - used;

                    String startIp = longToIp(1 | netaddr);
                    String usedIp = longToIp(ipnum | netaddr);
                    String nextIp = longToIp((ipnum + 1) | netaddr);
                    String endIp = longToIp(maxIpNum | netaddr);

                    if (ipnum >= maxIpNum) {
                        tunnelData.put("used_range", startIp + " ~ " + usedIp);
                        tunnelData.put("unused_range", "");
                    } else if (ipnum < 1) {
                        tunnelData.put("used_range", "");
                        tunnelData.put("unused_range", nextIp + " ~ " + endIp);
                    } else {
                        tunnelData.put("used_range", startIp + " ~ " + usedIp);
                        tunnelData.put("unused_range", nextIp + " ~ " + endIp);
                    }

                    tunnelData.put("used", String.valueOf(used));
                    tunnelData.put("unused", String.valueOf(unused));
                    tunnelData.put("range", startIp + " ~ " + endIp);
                }

                tunnels.add(tunnelData);
            }

            result.put("tunnelList", tunnels);

        } catch (Exception e) {
            result.put("success", "1");
            result.put("log", "x Failed to load database: " + e.getMessage());
        }

        return result;
    }

    private long ipToLong(String ipAddress) {
        try {
            byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
            long result = 0;
            for (byte b : bytes) {
                result = (result << 8) | (b & 0xFF);
            }
            return result;
        } catch (UnknownHostException e) {
            return 0;
        }
    }

    private String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF);
    }
}
