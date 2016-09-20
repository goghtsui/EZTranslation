/*
 * Copyright 2016 xiaofeng gogh
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
package com.gogh.plugin.easytranslation;

import com.gogh.plugin.common.ApiConfig;
import com.gogh.plugin.common.IString;
import com.gogh.plugin.common.LruCache;
import com.gogh.plugin.entity.ResultEntity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Future;

@SuppressWarnings("WeakerAccess")
public class TranslationRequest {

    @SuppressWarnings("SpellCheckingInspection")
    private static final Logger LOG = Logger.getInstance("#TranslationRequest");

    private static final TranslationRequest TRANSLATION_REQUEST = new TranslationRequest();

    private final LruCache<String, ResultEntity> mCache = new LruCache<>(200);
    private Future<?> mCurrentTask;

    private TranslationRequest() {
        LOG.debug("TranslationRequest constructor.");
    }

    public static TranslationRequest getInstance() {
        return TRANSLATION_REQUEST;
    }

    public void query(String query, Callback callback) {
        if (IString.isEmpty(query)) {
            if (callback != null) {
                callback.onQuery(query, null);
            }

            return;
        }

        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
            mCurrentTask = null;
        }

        ResultEntity cache;
        synchronized (mCache) {
            cache = mCache.get(query);
        }
        if (cache != null) {
            if (callback != null) {
                callback.onQuery(query, cache);
            }
        } else {
            mCurrentTask = ApplicationManager.getApplication()
                    .executeOnPooledThread(new QueryRequest(query, callback));
        }
    }

    private final class QueryRequest implements Runnable {

        private final String mQuery;
        private final Callback mCallback;

        QueryRequest(String query, Callback callback) {
            mQuery = query;
            mCallback = callback;
        }

        @Override
        public void run() {
            final String query = mQuery;
            CloseableHttpClient httpClient = HttpClients.createDefault();

            ResultEntity result;
            try {
                String url = getQueryUrl(query);
                HttpGet httpGet = new HttpGet(url);
                result = httpClient.execute(httpGet, new IResponseHandler());
                if (result != null && result.getErrorCode() == ResultEntity.ERROR_CODE_NONE) {
                    synchronized (mCache) {
                        mCache.put(query, result);
                    }
                }
            } catch (Exception e) {
                LOG.warn("query...", e);
                result = null;
            } finally {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            final ResultEntity postResult = result;
            ApplicationManager.getApplication().invokeLater(() -> {
                if (mCallback != null) {
                    mCallback.onQuery(query, postResult);
                }
            });
        }
    }

    private final class IResponseHandler implements ResponseHandler<ResultEntity> {

        @Override
        public ResultEntity handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity == null)
                    return null;

                String json = EntityUtils.toString(entity);
                LOG.info(json);

                try {
                    return new Gson().fromJson(json, ResultEntity.class);
                } catch (JsonSyntaxException e) {
                    LOG.warn(e);

                    ResultEntity result = new ResultEntity();
                    result.setErrorCode(ResultEntity.ERROR_CODE_RESTRICTED);

                    return result;
                }
            } else {
                String message = "Unexpected response status: " + status;
                LOG.warn(message);
                throw new ClientProtocolException(message);
            }
        }
    }

    static String getQueryUrl(String query) {
        String encodedQuery = "";
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String[] apiKey = ApiConfig.getAPISet();

        return IString.BASIC_URL + apiKey[0] + "&key=" + apiKey[1] + "&q=" + encodedQuery;
    }

    public interface Callback {
        void onQuery(String query, ResultEntity result);
    }

}
