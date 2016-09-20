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
package com.gogh.plugin.adapter;

import com.gogh.plugin.common.IString;
import com.gogh.plugin.easytranslation.TranslationRequest;
import com.gogh.plugin.entity.ResultEntity;
import com.gogh.plugin.iinterface.IDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> an adapter to render datas from online on view.</p>
 * <p> Created by <b>高晓峰</b> on 8/21/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/21/2016 do create. </li>
 */
@SuppressWarnings("WeakerAccess")
public class QueryOnlineAdapter {

    private int repeatCount = 0;

    /**
     * the size of history record list.
     */
    private static final int HISTORY_SIZE = 50;

    /**
     * history list.
     */
    private static final List<String> HISTORY = new ArrayList<>(HISTORY_SIZE);

    private final IDialog mIDialog;

    /**
     * the word you select currently.
     */
    private String selectedWord;

    /**
     * constructor
     *
     * @param view
     */
    public QueryOnlineAdapter(@NotNull IDialog view) {
        this.mIDialog = Objects.requireNonNull(view, "view cannot be null.");
    }

    @NotNull
    public List<String> getHistory() {
        return Collections.unmodifiableList(HISTORY);
    }

    public void query(@Nullable String word) {
        if (IString.isEmpty(word) || word.equals(selectedWord))
            return;

        word = word.trim();

        List<String> history = HISTORY;
        int index = history.indexOf(word);
        if (index != 0) {
            if (index > 0) {
                history.remove(index);
            }
            if (history.size() >= HISTORY_SIZE) {
                history.remove(HISTORY_SIZE - 1);
            }

            history.add(0, word);
            mIDialog.onUpdateHistory();
        }

        selectedWord = word;
        TranslationRequest.getInstance().query(word, (query, result) -> onPostResult(query, result));
    }

    /**
     * display translation result or error message.
     *
     * @param word   word.
     * @param result translation result.
     */
    private void onPostResult(String word, ResultEntity result) {
        if (IString.isEmpty(word) || !word.equals(selectedWord))
            return;

        selectedWord = null;
        String errorMessage = BalloonAdapter.getErrorMessage(result);
        if (errorMessage != null) {
            if (errorMessage.equals(IString.ERROR_INVALID_KEY) || errorMessage.equals(IString.ERROR_RESTRICTED)) {
                if (repeatCount < 3) {
                    selectedWord = "";
                    query(word);
                    repeatCount++;
                } else {
                    mIDialog.onError(errorMessage);
                }
            } else {
                mIDialog.onError(errorMessage);
            }
        } else {
            mIDialog.onCreateView(word, result);
        }
    }
}
