package com.gogh.plugin.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class OnlineEntity {

	@SerializedName(value = "key")
	private String key;
	@SerializedName(value = "value")
	private String[] values;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "OnlineEntity [key=" + key + ", values=" + Arrays.toString(values)
				+ "]";
	}

}
