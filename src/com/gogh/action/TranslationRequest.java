package com.gogh.action;

import com.gogh.common.MColor;
import com.gogh.common.MString;
import com.gogh.common.Number;
import com.gogh.entity.Translation;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> Description: an task used to translate english word by http request. </p>
 * <p> replace your information, for more details to see : http://fanyi.youdao.com/openapi?path=data-mode </p>
 * <p> Created by <b>高晓峰</b> on 8/15/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/15/2016. </li>
 */
public class TranslationRequest implements Runnable {

    /**
     * The editor of IDE.
     */
    private Editor mEditor;

    /**
     * English word , you want to translate.
     */
    private String mQueryWord;

    /**
     * construtor
     *
     * @param editor editor of IDE.
     * @param queryWord english word , you want to translate.
     */
    public TranslationRequest(Editor editor, String queryWord) {
        this.mEditor = editor;
        this.mQueryWord = queryWord;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            URI uri = createURI(mQueryWord);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(Number.DURATION.LONG)
                    .setConnectTimeout(Number.DURATION.LONG)
                    .setConnectionRequestTimeout(Number.DURATION.LONG).build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= Number.CODE.SUCCESS && status < Number.CODE.FAILED) {
                HttpEntity resEntity = response.getEntity();
                String json = EntityUtils.toString(resEntity, "UTF-8");
                Gson gson = new Gson();
                Translation translation = gson.fromJson(json, Translation.class);
                toast(translation.toString());
            } else {
                toast(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            toast(e.getMessage());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * create request uri.
     * @param word the word you want to translate.
     * @return request uri.
     *
     * @throws URISyntaxException
     */
    private URI createURI(String word) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost(MString.HOST)
                .setPath(MString.PATH)
                .addParameter(MString.PARAM_KEY_FROM, MString.KEY_FROM)
                .addParameter(MString.PARAM_KEY, MString.KEY)
                .addParameter(MString.PARAM_TYPE, MString.TYPE)
                .addParameter(MString.PARAM_VERSION, MString.VERSION)
                .addParameter(MString.PARAM_DOC_TYPE, MString.DOC_TYPE)
                .addParameter(MString.PARAM_CALL_BACK, MString.CALL_BACK)
                .addParameter(MString.PARAM_QUERY, word);
        return builder.build();
    }

    /**
     * display translate response.
     * @param result
     */
    private void toast(final String result) {
        ApplicationManager.getApplication().invokeLater(() -> {
            JBPopupFactory factory = JBPopupFactory.getInstance();
            factory.createHtmlTextBalloonBuilder(result, null, new JBColor(
                    new Color(MColor.JBColor.RED, MColor.JBColor.GREEN, MColor.JBColor.BLUE)
                    , new Color(MColor.NColor.RED, MColor.NColor.GREEN, MColor.NColor.BLUE)), null)
                    .setFadeoutTime(Number.DURATION.LONG)
                    .createBalloon()
                    .show(factory.guessBestPopupLocation(mEditor) , Balloon.Position.below);
        });
    }

}
