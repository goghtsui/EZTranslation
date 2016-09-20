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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by xiaofeng on 9/1/2016.
 */
public class SelectPlatform extends AnAction {

    JBPanel jbPanel;
    private JPanel mProcessPanel;

    public SelectPlatform(){
        super(IString.QUERY_ONLINE_ACTION_NAME, IString.TRANSLATE_NAME
                , IconLoader.getIcon(ICON.ICON_16));
        jbPanel = new JBPanel<>();
        mProcessPanel= new JPanel();
        GroupLayout mLayout = new GroupLayout(jbPanel);
        mLayout.setHorizontalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(mProcessPanel, JBUI.scale(200), GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        mLayout.setVerticalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(mProcessPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        jbPanel.setOpaque(false);
        jbPanel.setLayout(mLayout);
        jbPanel.add(mProcessPanel);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Balloon myBalloon = buildBalloon(jbPanel).setCloseButtonEnabled(false).createBalloon();
        myBalloon.show(JBPopupFactory.getInstance().guessBestPopupLocation(CommonDataKeys.EDITOR.getData(e.getDataContext())), Balloon.Position.below);
    }

    @NotNull
    private BalloonBuilder buildBalloon(JComponent component) {
        JBInsets BORDER_INSETS = JBUI.insets(20, 20, 20, 20);
        return JBPopupFactory.getInstance()
                .createDialogBalloonBuilder(component, null)
                .setHideOnClickOutside(true)
                .setShadow(true)
                .setBlockClicksThroughBalloon(true)
                .setRequestFocus(true)
                .setBorderInsets(BORDER_INSETS);
    }
}
