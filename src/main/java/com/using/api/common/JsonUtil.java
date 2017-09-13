package com.using.api.common;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonUtil {

	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String paramJson) {
		return JSON.parseObject(paramJson, Map.class);
	}

	public static String toString(Object result) {
		return JSON.toJSONString(result, SerializerFeature.WriteMapNullValue);
	}

	public static <T> Object convertValue(Object val, Class<T> targetClass) {
		return JSON.parseObject(val.toString(), targetClass);
	}

}
