package com.using.api.service;

import java.io.Serializable;

import org.springframework.stereotype.Service;

import com.using.api.core.APIMapping;

@Service
public class GoodsServiceImpl {

	// http://127.0.0.1:8080/api-gateway/api?method=api.goods.add&params={%22goods%22:{%22goodsName%22:%22aaa%22,%22goodsId%22:%22111%22},%22id%22:%2210%22}
	@APIMapping("api.goods.add")
	public Goods addGoods(Goods goods, Integer id) {
		return goods;
	}
	
	
	// http://127.0.0.1:8080/api-gateway/api?method=api.goods.get&params={%22id%22:%223333%22}
	@APIMapping("api.goods.get")
	public Goods getGoods(Integer id) {
		Goods goods = new Goods();
		goods.setGoodsId(String.valueOf(id));
		goods.setGoodsName("测试商品名称");
		return goods;
	}


	
	public static class Goods implements Serializable {
		private static final long serialVersionUID = 1L;
		private String goodsName;
		private String goodsId;

		public String getGoodsName() {
			return goodsName;
		}

		public void setGoodsName(String goodsName) {
			this.goodsName = goodsName;
		}

		public String getGoodsId() {
			return goodsId;
		}

		public void setGoodsId(String goodsId) {
			this.goodsId = goodsId;
		}
	}

}
