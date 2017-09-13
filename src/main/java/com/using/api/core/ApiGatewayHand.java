package com.using.api.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import com.alibaba.fastjson.JSON;
import com.using.api.common.ApiException;
import com.using.api.common.JsonUtil;
import com.using.api.core.ApiStore.ApiRunnable;
import com.using.api.service.GoodsServiceImpl.Goods;

public class ApiGatewayHand implements InitializingBean, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(ApiGatewayHand.class);
	private static final String METHOD = "method";
	private static final String PARAMS = "params";
	ApiStore apiStore;
	final ParameterNameDiscoverer parameterUtil;
	
	public ApiGatewayHand() {
		parameterUtil = new LocalVariableTableParameterNameDiscoverer();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		apiStore = new ApiStore(context);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		apiStore.loadApiFromSpringBeans();
	}

	/**
	 * api调用处理
	 * @param request
	 * @param response
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		String method = request.getParameter(METHOD);
		String params = request.getParameter(PARAMS);
		Object result;
		try {
			ApiRunnable apiRunnable = sysParamsValidate(request);
			logger.info(String.format("调用api:%s,参数:%s", method, params));
			Object[] args = buildParams(apiRunnable, params, request, response);
			result = handleSuccess(apiRunnable.run(args));
		} catch (ApiException e) {
			response.setStatus(500);
			result = handleError(e); // api异常
		} catch (InvocationTargetException e) {
			response.setStatus(500);
			result = handleError(e.getTargetException());
		} catch (Exception e) {
			response.setStatus(500);
			result = handleError(e);
		} 
		returnResult(result, response);
	}
	
	/**
	 * 成功处理
	 * @param result
	 * @return
	 */
	private Object handleSuccess(Object result) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("code", "0");
		resultMap.put("msg", "success");
		resultMap.put("data", result);
		return resultMap;
	}
	
	/**
	 * 错误处理
	 * @param throwable
	 * @return
	 */
	private Object handleError(Throwable throwable) {
		String code = "";
		String msg = "";
		if(throwable instanceof ApiException) {
			code = "001";
			msg = throwable.getMessage();
		} else {
			code = "002";
			msg = throwable.getMessage();
		}
		
		/*
		 * 构建返回值
		 */
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", code);
		result.put("msg", msg);
		
		//ByteArrayOutputStream out = new ByteArrayOutputStream();
		//PrintStream stream = new PrintStream(out);
		//throwable.printStackTrace(stream);
		//result.put("stack", out.toString()); // 错误堆栈
		return result;
	}


	/**
	 * 调用方法参数处理
	 * @param run
	 * @param paramsJson
	 * @param request
	 * @param response
	 * @return
	 * @throws ApiException
	 */
	private Object[] buildParams(ApiRunnable run, String paramsJson, HttpServletRequest request, HttpServletResponse response) throws ApiException {
		Map<String, Object> map = null;
		try {
			map = JsonUtil.toMap(paramsJson);
		} catch (Exception e) {
			throw new ApiException("params参数解析失败,请检查是否为合法的json格式");
		}
		if(map == null) {
			map = new HashMap<String, Object>();
		}
		
		Method method = run.getTargetMethod();
		List<String> paramNames = Arrays.asList(parameterUtil.getParameterNames(method));
		Class<?>[] paramTypes = method.getParameterTypes(); // 反射
		for (Map.Entry<String, Object> m : map.entrySet()) {
			if(!paramNames.contains(m.getKey())) {
				throw new ApiException("调用的接口不存在'" + m.getKey() + "'参数");
			}
		}
		
		Object[] args = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			if(paramTypes[i].isAssignableFrom(HttpServletRequest.class)) {
				args[i] = request;
			} else if(map.containsKey(paramNames.get(i))) {
				try {
					args[i] = convertJsonToBean(map.get(paramNames.get(i)), paramTypes[i]);
				} catch (Exception e) {
					throw new ApiException("指定参数格式错误或值错误：" + paramNames.get(i) + "．" + e.getMessage());
				}
			} else {
				args[i] = null;
			}
		}
		return args;
	}


	/**
	 * 参数类型转换
	 * @param val
	 * @param targetClass
	 * @return
	 * @throws Exception
	 */
	private <T> Object convertJsonToBean(Object val, Class<T> targetClass) throws Exception {
		Object result = null;
		if(val == null) {
			return null;
		} else if(Integer.class.equals(targetClass)) {
			result = Integer.parseInt(val.toString());
		} else if(Long.class.equals(targetClass)) {
			result = Long.parseLong(val.toString());
		} else if(Date.class.equals(targetClass)) {
			if(val.toString().matches("[0-9]+")) {
				result = new Date(Long.parseLong(val.toString()));
			} else {
				throw new IllegalArgumentException("日期必须为时间戳");
			}
		} else if(String.class.equals(targetClass)) {
			if(val instanceof String) {
				result = val; 
			} else {
				throw new IllegalArgumentException("转换目标类型为字符串");
			}
		} else {
			result = JsonUtil.convertValue(val, targetClass);
		}
		return result;
	}


	/**
	 * 系统级别参数严重
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	private ApiRunnable sysParamsValidate(HttpServletRequest request) throws ApiException {
		String method = request.getParameter(METHOD);
		String params = request.getParameter(PARAMS);
		ApiRunnable api;
		if(method == null || method.trim().equals("")) {
			throw new ApiException("参数method不能为空");
		} if(params == null || params.trim().equals("")) {
			throw new ApiException("参数params不能为空");
		}else if((api = apiStore.findApiRunnable(method)) == null) {
			throw new ApiException("调用的api不存在" + method);
		}
		return api;
	}

	
	/**
	 * 返回值处理
	 * @param result
	 * @param response
	 */
	private void returnResult(Object result, HttpServletResponse response) {
		try {
			String json = JsonUtil.toString(result);
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html/json;charset=UTF-8");
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pargma", "no-cache");
			response.setDateHeader("Expires", 0);
			if(json != null) {
				response.getWriter().write(json);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	
	public static void main(String[] args) {
		String str = "{\"goods\":{\"goodsName\":\"aaa\",\"goodsId\":\"111\"},\"id\":\"10\"}";
		Map<String, Object> map = JsonUtil.toMap(str);
		System.out.println(map);
		//UtilJson.convertValue(map.get("goods"), Goods.class);
		
		String str2 = "{\"goodsId\":\"111\",\"goodsName\":\"aaa\"}";
		Goods goods = JSON.parseObject(str2, Goods.class);
		System.out.println(goods.getGoodsName());
		System.out.println(goods.getGoodsId());
		//Goods goods = UtilJson.convertValue(str2, Goods.class);
	}
	
	
}
