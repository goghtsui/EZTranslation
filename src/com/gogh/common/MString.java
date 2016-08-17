package com.gogh.common;

/**
 * Created by user on 8/15/2016.
 */
public interface MString {

    /**
     * youdao translation domain.
     */
    String HOST = "fanyi.youdao.com";

    /**
     * youdao translation interface address.
     */
    String PATH = "/openapi.do";

    /**
     * interface param : application name.
     */
    String PARAM_KEY_FROM = "keyfrom";

    /**
     * interface param : apikey bound to application.
     */
    String PARAM_KEY = "key";

    /**
     * interface param : return type, default value : "data".
     */
    String PARAM_TYPE = "type";

    /**
     * interface param : response data type : xml、json、jsonp.
     */
    String PARAM_DOC_TYPE = "doctype";

    /**
     * interface param : default value "1.1".
     */
    String PARAM_VERSION = "version";

    /**
     * interface param : the word you want to translate.
     */
    String PARAM_QUERY = "q";

    /**
     * application name.
     */
    String KEY_FROM = "Easy-Translation";

    /**
     * apikey value.
     */
    String KEY = "1525768550";

    /**
     * type value.
     */
    String TYPE = "data";

    /**
     * doctype value.
     */
    String DOC_TYPE = "json";

    /**
     * version value.
     */
    String VERSION = "1.1";

    String PARAM_CALL_BACK = "callback";
    String CALL_BACK = "show";

}
