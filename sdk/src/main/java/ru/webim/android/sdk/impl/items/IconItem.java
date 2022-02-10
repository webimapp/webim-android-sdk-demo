package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

 class IconItem {
	@SerializedName("color")
	private String color;
	@SerializedName("shape")
	private String shape;
	
	 String getColor() {
		return color;
	}
	
	 String getShape() {
		return shape;
	}
	 IconItem() {
		// Need for Gson No-args fix
	}
}
