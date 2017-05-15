package com.bopinjia.customer.bean;

public class DistributionLevelBean {

	private String img;
	private String name;
	private String price;
	private String month;
	private String market;
	private String type;
	private String id;
	
	private String level;
	
	private String nowlevel;
	
	
	public String getNowlevel() {
		return nowlevel;
	}

	public void setNowlevel(String nowlevel) {
		this.nowlevel = nowlevel;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getEntertype() {
		return entertype;
	}

	public void setEntertype(String entertype) {
		this.entertype = entertype;
	}

	private String entertype;

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
