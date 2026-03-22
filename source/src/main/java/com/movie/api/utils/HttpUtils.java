package com.movie.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Slf4j
public class HttpUtils {
	final ObjectMapper mapper = new ObjectMapper();
	Logger logger = LogManager.getLogger(HttpUtils.class);
	private static HttpUtils instance;

	public static HttpUtils getInstance() {
		if (instance == null) {
			instance = new HttpUtils();
		}
		return instance;
	}

	public static String md5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			log.error(e.getMessage(),e);
		}
		return null;
	}


}
