package com.bopinjia.customer.adapter;

import android.widget.ImageView;

public class ProductListModel {

	private String name;
	private String count;
	private String sale_price;
	private String market_price;
	private String thumbnails;
	private String isShip;
	private String skuid;
	public String getRealStock() {
		return RealStock;
	}

	public void setRealStock(String realStock) {
		RealStock = realStock;
	}

	private String RealStock;

	public String getSkuid() {
		return skuid;
	}

	public void setSkuid(String skuid) {
		this.skuid = skuid;
	}

	public String getIsShip() {
		return isShip;
	}

	public void setIsShip(String isShip) {
		this.isShip = isShip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getSale_price() {
		return sale_price;
	}

	public void setSale_price(String sale_price) {
		this.sale_price = sale_price;
	}

	public String getMarket_price() {
		return market_price;
	}

	public void setMarket_price(String market_price) {
		this.market_price = market_price;
	}

	public String getThumbnails() {
		return thumbnails;
	}

	public void setThumbnails(String thumbnails) {
		this.thumbnails = thumbnails;
	}
}
