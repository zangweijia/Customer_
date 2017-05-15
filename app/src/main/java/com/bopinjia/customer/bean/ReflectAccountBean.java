package com.bopinjia.customer.bean;

import java.util.List;

public class ReflectAccountBean {

	private String UserAccountTypeId;

	private String UserAccounts;

	private String UserAccountName;

	private String UserAccountRealName;
	
	private String UserAccountPhone;
	
	private String UserAccountId;

	public String getUserAccountId() {
		return UserAccountId;
	}

	public void setUserAccountId(String userAccountId) {
		UserAccountId = userAccountId;
	}

	public String getUserAccountPhone() {
		return UserAccountPhone;
	}

	public void setUserAccountPhone(String userAccountPhone) {
		UserAccountPhone = userAccountPhone;
	}

	public String getUserAccountTypeId() {
		return UserAccountTypeId;
	}

	public void setUserAccountTypeId(String userAccountTypeId) {
		UserAccountTypeId = userAccountTypeId;
	}

	public String getUserAccounts() {
		return UserAccounts;
	}

	public void setUserAccounts(String userAccounts) {
		UserAccounts = userAccounts;
	}

	public String getUserAccountName() {
		return UserAccountName;
	}

	public void setUserAccountName(String userAccountName) {
		UserAccountName = userAccountName;
	}

	public String getUserAccountRealName() {
		return UserAccountRealName;
	}

	public void setUserAccountRealName(String userAccountRealName) {
		UserAccountRealName = userAccountRealName;
	}

}
