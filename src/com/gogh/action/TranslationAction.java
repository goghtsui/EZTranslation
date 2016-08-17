package com.gogh.action;

import com.gogh.common.Number;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.apache.http.util.TextUtils;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> Description: an action for translate. </p>
 * <p> Created by <b>高晓峰</b> on 8/15/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/15/2016. </li>
 */
public class TranslationAction extends AnAction {

    /**
     * last clicked time.
     */
    private long mLastClickedTime;

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        if (!isFastClick()) {
            translate(actionEvent);
        }
    }

    /**
     * calculate the interval time between two clicks.
     *
     * @return
     */
    public boolean isFastClick() {
        long time = System.currentTimeMillis();
        long interval = time - mLastClickedTime;
        if (0 < interval && interval < Number.DURATION.SHORT) {
            return true;
        }
        mLastClickedTime = time;
        return false;
    }

    /**
     * translate the english word.
     * @param event
     */
    private void translate(AnActionEvent event) {
        Editor mEditor =  event.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }
        SelectionModel model = mEditor.getSelectionModel();
        String selectedWord = model.getSelectedText();
        if (TextUtils.isEmpty(selectedWord)) {
            return;
        }
        String queryText = formatWord(selectedWord);
        new Thread(new TranslationRequest(mEditor, queryText)).start();
    }

    /**
     * format word.
     * @param word the word you want to translate.
     * @return
     */
    public String formatWord(String word) {
        String temp = word.replaceAll("_"," ");
        if (temp.equals(temp.toUpperCase())) {
            return temp;
        }
        String result = temp.replaceAll("([A-Z])", " $0");
        return result;
    }

}
