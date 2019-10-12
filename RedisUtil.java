package com.integration.util;

import redis.clients.jedis.Jedis;

public class RedisUtil {
    public static String getPositsByCode(String code) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);  
        //jedis.auth("rts88441400");
        String position = jedis.get(code);
        jedis.close(); 
        return position;
    }    
}
