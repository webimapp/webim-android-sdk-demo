package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

public final class VisitorItem {
	@SerializedName("fields")
	private ItemVisitorFields fields;
	@SerializedName("id")
	private String id;
	@SerializedName("icon")
	private IconItem icon;
	
	public CharSequence getName() {
		return fields.getName();
	}
	public ItemVisitorFields getFields() {
		return fields;
	}
	public IconItem getIcon() {
		return icon;
	}
	public VisitorItem() {
		// Need for Gson No-args fix
	}
	public String getId() {
		return id;
	}
	@Override
	public String toString() {
		return super.toString();
	}

	public static final class ItemVisitorFields {
        @SerializedName("phone")
        private String phone;
        @SerializedName("name")
        private String name;
        @SerializedName("email")
        private String email;

        public ItemVisitorFields() {
            // Need for Gson No-args fix
        }

        public ItemVisitorFields(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }
}
