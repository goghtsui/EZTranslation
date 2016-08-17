package com.gogh.entity;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> Description: response entity. </p>
 * <p> Created by <b>高晓峰</b> on 8/15/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/15/2016. </li>
 */
public class Translation {

    /**
     * 美式发音
     */
    private final static String US_PHONETIC = "us-phonetic";

    /**
     * 英式发音
     */
    private final static String UK_PHONETIC = "uk-phonetic";

    /**
     * 拼音
     */
    private final static String PHONETIC = "phonetic";

    /**
     * 解释
     */
    private final static String EXPLAINS = "explains";

    private final static int SUCCESS = 0;
    private final static int QUERY_STRING_TOO_LONG = 20;
    private final static int CAN_NOT_TRANSLATE = 30;
    private final static int INVALID_LANGUAGE = 40;
    private final static int INVALID_KEY = 50;
    private final static int NO_RESULT = 60;

    private String query;
    private int errorCode;
    private String[] translation;
    private Map<String, Object> basic;
    private List<Map<String, Object>> web;

    public String[] getTranslation() {
        return translation;
    }

    public void setTranslation(String[] translation) {
        this.translation = translation;
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

    public Map<String, Object> getBasic() {
        return basic;
    }

    public void setBasic(Map<String, Object> basic) {
        this.basic = basic;
    }

    public List<Map<String, Object>> getWeb() {
        return web;
    }

    public void setWeb(List<Map<String, Object>> web) {
        this.web = web;
    }

    private String getErrorMessage() {
        switch (errorCode) {
            case SUCCESS:
                return "成功";
            case QUERY_STRING_TOO_LONG:
                return "要翻译的内容过长";
            case CAN_NOT_TRANSLATE:
                return "无法进行有效的翻译";
            case INVALID_LANGUAGE:
                return "不支持的语言类型";
            case INVALID_KEY:
                return "无效的key";
            case NO_RESULT:
                return "无词典结果";
        }
        return "可能是火星文字";
    }

    private String getPhonetic() {
        if (basic == null) {
            return null;
        }
        String phonetic = null;
        String us_phonetic = (String) basic.get(US_PHONETIC);
        String uk_phonetic = (String) basic.get(UK_PHONETIC);
        if (us_phonetic == null && uk_phonetic == null) {
            phonetic = "拼音：[" + basic.get(PHONETIC) + "];";
        } else {
            if (us_phonetic != null) {
                phonetic = "美式：[" + us_phonetic + "];";
            }
            if (uk_phonetic != null) {
                if (phonetic == null) {
                    phonetic = "";
                }
                phonetic = phonetic + "英式：[" + uk_phonetic + "];";
            }
        }
        return phonetic;
    }

    private String getExplains() {
        if (basic == null) {
            return null;
        }
        String result = null;
        List<String> explains = (List<String>) basic.get(EXPLAINS);
        if (explains.size() > 0) {
            result = "";
        }
        for (String explain : explains) {
            result += explain + "\n";
        }
        return result;
    }

    private String getTranslationResult() {
        if (translation == null) {
            return null;
        }
        String result = null;
        if (translation.length > 0) {
            result = "";
        }
        for (String r : translation) {
            result += (r + ";");
        }
        return result;
    }

    private String getWebResult() {
        if (web == null) {
            return null;
        }
        String result = null;
        if (web.size() > 0) {
            result = "";
        }
        for (Map<String, Object> map : web) {
            String key = (String) map.get("key");
            result += (key + " : ");
            List<String> value = (List<String>) map.get("value");
            for (String str : value) {
                result += (str + ",");
            }
            result += "\n";
        }
        return result;
    }

    @Override
    public String toString() {
        String string = null;
        if (errorCode != SUCCESS) {
            string = "错误代码：" + errorCode + "\n" + getErrorMessage();
        } else {
            String translation = getTranslationResult();
            if (translation != null) {
                translation = translation.substring(0, translation.length() - 1);
                if (!translation.equals(query)) {
                    string = (query + ":" + getTranslationResult() + "\n");
                }
            }
            if (getPhonetic() != null) {
                if (string == null) {
                    string = "";
                }
                string += (getPhonetic() + "\n");
            }
            if (getExplains() != null) {
                if (string == null) {
                    string = "";
                }
                string += (getExplains());
            }
            if (getWebResult() != null) {
                if (string == null) {
                    string = "";
                }
                string += "网络释义：\n";
                string += (getWebResult());
            }
        }
        if (string == null) {
            string = "抱歉,您选的内容：" + query + "\n小的翻译不了...";
        }
        return string;
    }

}
