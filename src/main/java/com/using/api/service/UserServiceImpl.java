package com.using.api.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.using.api.core.APIMapping;
import com.using.api.service.GoodsServiceImpl.Goods;

@Service
public class UserServiceImpl {

	
	@APIMapping("api.goods.info")
	public Map<String, Object> getGoodsInfo(Integer id, String bbb) {
		System.out.println(id + "   " + bbb);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ssss", "sssf");
		map.put("bbbb", "bbb");
		Goods goods = new Goods();
		goods.setGoodsName("goodsName");
		map.put("goods", goods);
		return map;
	}
	
	
}
