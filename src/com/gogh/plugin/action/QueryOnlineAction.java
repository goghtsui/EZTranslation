package com.gogh.plugin.action;

import com.gogh.plugin.common.ICON;
import com.gogh.plugin.common.IString;
import com.gogh.plugin.ui.QueryOnlineWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.IconLoader;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> An action used to translate words and display the result. In a popup window you can input a word,
 * click button "Query" to translate the word online. </p>
 * <p> Created by <b>高晓峰</b> on 8/15/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/15/2016 do create. </li>
 * <li> 高晓峰 on 8/21/2016 do add the action. </li>
 */
public class QueryOnlineAction extends AnAction implements DumbAware {

    /**
     * constructor
     */
    public QueryOnlineAction() {
        super(IString.QUERY_ONLINE_ACTION_NAME, IString.TRANSLATE_NAME
                , IconLoader.getIcon(ICON.ICON_16));
    }

    /**
     * An action event callback.
     * @param e an action event.
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        QueryOnlineWindow.getInstance().show(e.getProject());
    }

}
