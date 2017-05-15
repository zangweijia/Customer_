package com.bopinjia.customer.bean;

public class LoginBean {

	private String Result;
	private String Message;
	private DataBean Data;

	public String getResult() {
		return Result;
	}

	public void setResult(String result) {
		Result = result;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public DataBean getData() {
		return Data;
	}

	public void setData(DataBean data) {
		Data = data;
	}

	public class DataBean {

		private String UserId;
		private String RegisterPhone;

		public String getUserId() {
			return UserId;
		}

		public void setUserId(String userId) {
			UserId = userId;
		}

		public String getRegisterPhone() {
			return RegisterPhone;
		}

		public void setRegisterPhone(String registerPhone) {
			RegisterPhone = registerPhone;
		}

		public String getNickName() {
			return NickName;
		}

		public void setNickName(String nickName) {
			NickName = nickName;
		}


		public int getUserLevel() {
			return UserLevel;
		}

		public void setUserLevel(int userLevel) {
			UserLevel = userLevel;
		}

		public String getHeadPortrait() {
			return HeadPortrait;
		}

		public void setHeadPortrait(String headPortrait) {
			HeadPortrait = headPortrait;
		}

		public String getSex() {
			return Sex;
		}

		public void setSex(String sex) {
			Sex = sex;
		}

		public String getUserqm() {
			return Userqm;
		}

		public void setUserqm(String userqm) {
			Userqm = userqm;
		}

		private String NickName;
		private int UserLevel;
		private String HeadPortrait;
		private String Sex;
		private String Userqm;

	}

}
