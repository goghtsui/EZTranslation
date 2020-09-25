/*
 * Copyright 2016 Yuyou Chow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gogh.plugin.translator;

import com.gogh.plugin.common.ApiConfig;
import com.gogh.plugin.entity.ResultEntity;
import com.google.gson.Gson;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ResourceUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * Created by zyuyou on 16/6/27.
 * Modify by xiaofeng gogh 2016/09/01
 *
 * <p>
 * https://github.com/Skykai521/ECTranslation/blob/master/src/RequestRunnable.java
 *
 * @author loucyin
 */
public class YoudaoTranslator extends TranslatorEx {

    private static final String[] DEFAULT_API_KEY_NAME = {"Easy-Translation", "Dr-Dictionary"};
    private static final String[] DEFAULT_API_KEY_VALUE = {"1525768550", "2053892299"};

    private static ResultEntity resultEntity = null;

    static final String HTTP_STYLE;

    static {
        String css;
        try {
            css = ResourceUtil.loadText(ResourceUtil.getResource(Translator.class, "/css", "youdao.css"));
        } catch (IOException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
        HTTP_STYLE = "<style type=\"text/css\">\n" + css + "</style>\n";
    }

    @NotNull
    @Override
    public String getMenuItem() {
        return "YouDao web platform.";
    }

    @NotNull
    @Override
    public String getTitle() {
        return "Translation";
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icon/youdao-16.png");
    }

    @Nullable
    @Override
    public String fetchInfo(String query) {
        return TranslatorUtil.fetchInfo(query, this);
    }

    @Nullable
    @Override
    public ResultEntity fetchDocInfo(String query) {
        resultEntity = TranslatorUtil.fetchEntity(query, this);
        return resultEntity;
    }

    @Nullable
    @Override
    public String getExternalUrl(String query) {
        try {
            String finalQuery = URLEncoder.encode(query, "UTF-8");
            return "http://dict.youdao.com/w/" + finalQuery + "/#keyfrom=dict2.top";
        } catch (UnsupportedEncodingException ignore) {
        }
        return null;
    }

    @NotNull
    @Override
    public URI createUrl(String query) throws URISyntaxException {
        String[] apiKey = ApiConfig.getAPISet();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost("fanyi.youdao.com")
                .setPath("/openapi.do")
                .addParameter("keyfrom", apiKey[0])
                .addParameter("key", apiKey[1])
                .addParameter("type", "data")
                .addParameter("version", "1.1")
                .addParameter("doctype", "json")
                .addParameter("q", query);
        return builder.build();
    }

    @Nullable
    @Override
    public String generateSuccess(HttpEntity entity) throws IOException {
        String json = EntityUtils.toString(entity, "UTF-8");
        Gson gson = new Gson();
        YoudaoTranslation result = gson.fromJson(json, YoudaoTranslation.class);
        return decorateHtml(result.toString());
    }

    @Nullable
    @Override
    public String generateFail(HttpResponse response) {
        return response.getStatusLine().getReasonPhrase();
    }

    @NotNull
    private static String decorateHtml(@NotNull String retrievedHtml) {
        return "<html>" + HTTP_STYLE + "<body>" + retrievedHtml + "</body></html>";
    }
}
