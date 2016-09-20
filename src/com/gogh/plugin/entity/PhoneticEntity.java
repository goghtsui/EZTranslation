package com.gogh.plugin.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class PhoneticEntity {

	@SerializedName(value = "phonetic")
	private String phonetic;
	@SerializedName(value = "uk-phonetic")
	private String phoneticUK;
	@SerializedName(value = "us-phonetic")
	private String phoneticUS;
	@SerializedName(value = "explains")
	private String[] explains;

	public String getPhonetic() {
		return phonetic;
	}

	public void setPhonetic(String phonetic) {
		this.phonetic = phonetic;
	}

	public String getPhoneticUK() {
		return phoneticUK;
	}

	public void setPhoneticUK(String phoneticUK) {
		this.phoneticUK = phoneticUK;
	}

	public String getPhoneticUS() {
		return phoneticUS;
	}

	public void setPhoneticUS(String phoneticUS) {
		this.phoneticUS = phoneticUS;
	}

	public String[] getExplains() {
		return explains;
	}

	public void setExplains(String[] explains) {
		this.explains = explains;
	}

	@Override
	public String toString() {
		return "PhoneticEntity [phonetic=" + phonetic + ", phoneticUK="
				+ phoneticUK + ", phoneticUS=" + phoneticUS + ", explains="
				+ Arrays.toString(explains) + "]";
	}

}
