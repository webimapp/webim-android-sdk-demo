package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

public final class OperatorItem {

	@SerializedName("departmentKeys")
	private String[] departmentKeys;
	@SerializedName("avatar")
	private String avatar;
	@SerializedName("fullname")
	private String fullname;
	@SerializedName("id")
	private String id;
	@SerializedName("title")
	private String title;
	@SerializedName("additionalInfo")
	private String info;

	public OperatorItem() {
		// Need for Gson No-args fix
	}

	public String[] getDepartmentKeys() {
		return departmentKeys;
	}

	public String getAvatar() {
		return avatar;
	}

	public String getFullname() {
		return fullname;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getInfo() {
		return info;
	}
}
