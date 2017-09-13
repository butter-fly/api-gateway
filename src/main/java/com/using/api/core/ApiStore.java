package com.using.api.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

public class ApiStore {
	private ApplicationContext applicationContext;
	private HashMap<String, ApiRunnable> apiMap = new HashMap<String, ApiRunnable>();

	public ApiStore(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext);
		this.applicationContext = applicationContext;
	}
	
	public void loadApiFromSpringBeans() {
		String[] names = applicationContext.getBeanDefinitionNames();
		Class<?> type;
		for (String name : names) {
			type = applicationContext.getType(name);
			for (Method m : type.getDeclaredMethods()) {
				APIMapping apiMapping = m.getAnnotation(APIMapping.class);
				if(apiMapping != null) {
					addApiItem(apiMapping, name, m);
				}
			}
		}
	}
	
	public ApiRunnable findApiRunnable(String apiName) {
		return apiMap.get(apiName);
	}

	public ApiRunnable findApiRunnable(String apiName, String version) {
		return apiMap.get(apiName + "_" + version);
	}
	
	private void addApiItem(APIMapping apiMapping, String beanName, Method method) {
		ApiRunnable apiRunnable = new ApiRunnable();
		apiRunnable.apiName = apiMapping.value();
		apiRunnable.targetMethod = method;
		apiRunnable.targetName = beanName;
		apiMap.put(apiMapping.value(), apiRunnable);
	}
	
	public List<ApiRunnable> findApiRunnables(String apiName) {
		if(apiName == null) {
			throw new IllegalArgumentException("api name must not null");
		}
		List<ApiRunnable> list = new ArrayList<ApiRunnable>(20);
		for (ApiRunnable api : apiMap.values()) {
			if(api.apiName.equals(apiName)) {
				list.add(api);
			}
		}
		return list;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public class ApiRunnable {
		String apiName; // api名称
		String targetName; // ioc bean name
		Object target; // 对象实例
		Method targetMethod; // 目标方法
		
		public Object run(Object...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if(target == null) {
				target = applicationContext.getBean(targetName); // 使用spring ioc获取bean对象
			}
			return targetMethod.invoke(target, args);
		}
		
		public Class<?>[] getParamTypes() {
			return targetMethod.getParameterTypes();
		}

		public String getApiName() {
			return apiName;
		}

		public String getTargetName() {
			return targetName;
		}

		public Object getTarget() {
			return target;
		}

		public Method getTargetMethod() {
			return targetMethod;
		} 
	}

}
