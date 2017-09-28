package com.shawn;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.util.JedisClusterCRC16;

import java.util.*;

public class Main {

    public static void main(String[] args) {
	// write your code here
        System.out.println("start");
        String host = "10.111.24.144";
        String port = "9000";
        String keys = "PRODUCT_SERVER*";
        if (args != null && args.length == 3) {
            host = args[0];
            port = args[1];
            keys = args[2];
        }
        delKeys(new HostAndPort(host, Integer.parseInt(port)), keys);
        System.out.println("end");
    }

    public static void delKeys(HostAndPort hostAndPort, String keysPattern) {
        Map<String, JedisPool> clusterNodes = new JedisCluster(hostAndPort).getClusterNodes();
        System.out.println("nodes:" + clusterNodes);
        for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
            Jedis jedis = entry.getValue().getResource();
            if (!jedis.info("replication").contains("role:slave")) {
                Set<String> keys = jedis.keys(keysPattern);
                System.out.println("key sizes:"+keys.size());
                if (keys.size() > 0) {
                    Map<Integer, List<String>> map = new HashMap<>(6600);
                    for (String key : keys) {
                        System.out.println("deleting key:"+key);
                        int slot = JedisClusterCRC16.getSlot(key);//cluster模式执行多key操作的时候，这些key必须在同一个slot上，不然会报:JedisDataException: CROSSSLOT Keys in request don't hash to the same slot
                        //按slot将key分组，相同slot的key一起提交
                        if (map.containsKey(slot)) {
                            map.get(slot).add(key);
                        } else {
                            List<String> keyList = new ArrayList<String>();
                            keyList.add(key);
                            map.put(slot, keyList);
                        }
                    }
                    for (Map.Entry<Integer, List<String>> integerListEntry : map.entrySet()) {
                        jedis.del(integerListEntry.getValue().toArray(new String[integerListEntry.getValue().size()]));
                    }
                }
            }
        }
    }
}
