package com.integration.util;

import java.util.UUID;

/**
 * 获取随机uuid
 * 
 * @author yanganshi
 * @time 2019-7-9 16:03:06
 */
public class UUIDUtile {
	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}
}

