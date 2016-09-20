package com.gogh.plugin.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
public class ResultEntity {

    public static final int ERROR_CODE_NONE = 0;
    public static final int ERROR_CODE_QUERY_TOO_LONG = 20;
    public static final int ERROR_CODE_FAIL = 30;
    public static final int ERROR_CODE_UNSUPPORTED_LANG = 40;
    public static final int ERROR_CODE_INVALID_KEY = 50;
    public static final int ERROR_CODE_NO_RESULT = 60;
    public static final int ERROR_CODE_RESTRICTED = -10;

    @SerializedName("query")
    private String query;
    @SerializedName("errorCode")
    private int errorCode;
    @SerializedName("translation")
    private String[] translation;

    @SerializedName("basic")
    private PhoneticEntity phoneticEntity;
    @SerializedName("web")
    private OnlineEntity[] onlineEntities;

    public ResultEntity() {
    }

    public ResultEntity(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String[] getTranslation() {
        return translation;
    }

    public void setTranslation(String[] translation) {
        this.translation = translation;
    }

    public PhoneticEntity getPhoneticEntity() {
        return phoneticEntity;
    }

    public void setPhoneticEntity(PhoneticEntity phoneticEntity) {
        this.phoneticEntity = phoneticEntity;
    }

    public OnlineEntity[] getOnlineEntities() {
        return onlineEntities;
    }

    public void setOnlineEntities(OnlineEntity[] onlineEntities) {
        this.onlineEntities = onlineEntities;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + errorCode;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResultEntity other = (ResultEntity) obj;
        if (errorCode != other.errorCode)
            return false;
        if (query == null) {
            if (other.query != null)
                return false;
        } else if (!query.equals(other.query))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ResultEntity [query=" + query + ", errorCode=" + errorCode
                + ", translation=" + Arrays.toString(translation)
                + ", phoneticEntity=" + phoneticEntity + ", onlineEntities="
                + Arrays.toString(onlineEntities) + "]";
    }

}
