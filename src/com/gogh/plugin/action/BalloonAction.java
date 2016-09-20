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
package com.gogh.plugin.action;

import com.gogh.plugin.common.ICON;
import com.gogh.plugin.common.IString;
import com.gogh.plugin.config.BalloonConfig;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.IconLoader;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> An action used to translate words. Display the translation result in a balloon.</p>
 * <p> Created by <b>高晓峰</b> on 8/15/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/15/2016 do create. </li>
 */
public class BalloonAction extends AnAction {

    /**
     * constructor
     */
    public BalloonAction() {
        super(IString.BALLOON_ACTION_NAME, IString.TRANSLATE_NAME, IconLoader.getIcon(ICON.ICON_16));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = getEditor(e);
        String selectedText = IString.splitWord(getSelectedText(e));
        if (editor != null && !IString.isEmpty(selectedText)) {
            new BalloonConfig(editor).showAndQuery(selectedText);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(!IString.isEmpty(getSelectedText(e)));
    }

    /**
     * get selected words you want to translate.
     * @param e an action event.
     * @return
     */
    private String getSelectedText(AnActionEvent e) {
        Editor editor = getEditor(e);
        String selectedText = null;
        if (editor != null) {
            selectedText = editor.getSelectionModel().getSelectedText();
        }
        return selectedText;
    }

    /**
     * get an editor in context.
     * @param e an action event.
     * @return
     */
    private Editor getEditor(AnActionEvent e) {
        return CommonDataKeys.EDITOR.getData(e.getDataContext());
    }

}
