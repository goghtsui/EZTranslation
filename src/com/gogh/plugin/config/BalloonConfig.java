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
package com.gogh.plugin.config;

import com.gogh.plugin.adapter.BalloonAdapter;
import com.gogh.plugin.adapter.QueryOnlineAdapter;
import com.gogh.plugin.common.ICON;
import com.gogh.plugin.common.IString;
import com.gogh.plugin.common.ProcessIcon;
import com.gogh.plugin.entity.ResultEntity;
import com.gogh.plugin.iinterface.IDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.BalloonImpl;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.AnimatedIcon;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> A balloon to display the translation content.</p>
 * <p> Created by 高晓峰 on 8/15/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/21/2016 do mofity. </li>
 */
@SuppressWarnings("WeakerAccess")
public class BalloonConfig implements IDialog {

    private static final Icon ICON_SEARCH = IconLoader.getIcon(ICON.ICON_SEARCH);

    private static final int MIN_WIDTH = JBUI.scale(200);
    private static final int MIN_HEIGHT = JBUI.scale(50);
    private static final int MAX_SIZE = JBUI.scale(600);
    private static final JBInsets BORDER_INSETS = JBUI.insets(20, 20, 20, 20);

    private final JBPanel mContentPanel;
    private final GroupLayout mLayout;

    private Balloon myBalloon;

    private final QueryOnlineAdapter mQueryOnlineAdapter;

    private final Editor mEditor;
    private JPanel mProcessPanel;
    private AnimatedIcon mProcessIcon;

    public BalloonConfig(@NotNull Editor mEditor) {
        this.mEditor = Objects.requireNonNull(mEditor, "editor cannot be null.");

        mContentPanel = new JBPanel<>();
        mLayout = new GroupLayout(mContentPanel);
        mContentPanel.setOpaque(false);
        mContentPanel.setLayout(mLayout);
        mProcessPanel.setOpaque(false);

        mLayout.setHorizontalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(mProcessPanel, MIN_WIDTH, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        mLayout.setVerticalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(mProcessPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        mContentPanel.add(mProcessPanel);
        mProcessIcon.resume();

        mQueryOnlineAdapter = new QueryOnlineAdapter(this);
    }

    private void createUIComponents() {
        mProcessIcon = new ProcessIcon();
    }

    @NotNull
    private BalloonBuilder buildBalloon() {
        return JBPopupFactory.getInstance()
                .createDialogBalloonBuilder(mContentPanel, null)
                .setHideOnClickOutside(true)
                .setShadow(true)
                .setBlockClicksThroughBalloon(true)
//                .setRequestFocus(false)
                .setBorderInsets(BORDER_INSETS);
    }

    public void showAndQuery(@NotNull String queryText) {
        myBalloon = buildBalloon().setCloseButtonEnabled(false).createBalloon();
        myBalloon.show(JBPopupFactory.getInstance().guessBestPopupLocation(mEditor), Balloon.Position.below);
        mQueryOnlineAdapter.query(Objects.requireNonNull(queryText, "the word you want to translate cannot be null."));
    }

    @Override
    public void onUpdateHistory() {
        // do nothing
    }

    @Override
    public void onCreateView(@NotNull String query, @NotNull ResultEntity result) {
        if (this.myBalloon != null) {
            if (this.myBalloon.isDisposed()) {
                return;
            }

            this.myBalloon.hide(true);
        }

        mContentPanel.remove(0);
        mProcessIcon.suspend();
        mProcessIcon.dispose();

        JTextPane resultText = new JTextPane();
        resultText.setEditable(false);
        resultText.setBackground(UIManager.getColor(IString.PANEL_BACKGROUND));
        resultText.setFont(JBUI.Fonts.create(IString.JBUI_FONT_YAHEI, JBUI.scaleFontSize(14)));

        BalloonAdapter.insertQueryResultText(resultText.getDocument(), result);
        resultText.setCaretPosition(0);

        JBScrollPane scrollPane = new JBScrollPane(resultText);
        scrollPane.setBorder(new JBEmptyBorder(0));
        scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
        scrollPane.setHorizontalScrollBar(scrollPane.createHorizontalScrollBar());

        mLayout.setHorizontalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(scrollPane, MIN_WIDTH, GroupLayout.DEFAULT_SIZE, MAX_SIZE));
        mLayout.setVerticalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(scrollPane, MIN_HEIGHT, GroupLayout.DEFAULT_SIZE, MAX_SIZE));
        mContentPanel.add(scrollPane);

        final BalloonImpl balloon = (BalloonImpl) buildBalloon().createBalloon();
        RelativePoint showPoint = JBPopupFactory.getInstance().guessBestPopupLocation(mEditor);
//        createSearchButton(balloon, showPoint);
        balloon.show(showPoint, Balloon.Position.below);

        // auto refresh masking scroll bar.
        ApplicationManager.getApplication().invokeLater(() -> balloon.revalidate());
    }

    /*private void createSearchButton(final BalloonImpl balloon, final RelativePoint showPoint) {
        balloon.setActionProvider(new BalloonImpl.ActionProvider() {
            private BalloonImpl.ActionButton myPinButton;

            @NotNull
            public List<BalloonImpl.ActionButton> createActions() {
                myPinButton = balloon.new ActionButton(ICON_SEARCH, ICON_SEARCH, null,
                        mouseEvent -> {
                            if (mouseEvent.getClickCount() == 1) {
                                balloon.hide(true);
                                QueryOnlineWindow.getInstance().show(mEditor.getProject());
                            }
                        });

                return Collections.singletonList(myPinButton);
            }

            public void layout(@NotNull Rectangle lpBounds) {
                if (myPinButton.isVisible()) {
                    int iconWidth = ICON_SEARCH.getIconWidth();
                    int iconHeight = ICON_SEARCH.getIconHeight();
                    int margin = JBUI.scale(3);
                    int x = lpBounds.x + lpBounds.width - iconWidth - margin;
                    int y = lpBounds.y + margin;

                    Rectangle rectangle = new Rectangle(x, y, iconWidth, iconHeight);
                    Insets border = balloon.getShadowBorderInsets();
                    rectangle.x -= border.left;

                    int showX = showPoint.getPoint().x;
                    int showY = showPoint.getPoint().y;
                    // offset
                    int offset = JBUI.scale(1);
                    boolean atRight = showX <= lpBounds.x + offset;
                    boolean atLeft = showX >= (lpBounds.x + lpBounds.width - offset);
                    boolean below = lpBounds.y >= showY;
                    boolean above = (lpBounds.y + lpBounds.height) <= showY;
                    if (atRight || atLeft || below || above) {
                        rectangle.y += border.top;
                    }

                    myPinButton.setBounds(rectangle);
                }
            }
        });
    }*/

    @Override
    public void onError(@NotNull String error) {
        if (myBalloon == null)
            return;

        mContentPanel.remove(0);
        mProcessIcon.suspend();
        mProcessIcon.dispose();

        JBLabel label = new JBLabel();
        label.setFont(JBUI.Fonts.label(16));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setText(IString.LOADING);

        mLayout.setHorizontalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(label, MIN_WIDTH, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        mLayout.setVerticalGroup(mLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(label, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        label.setForeground(new JBColor(new Color(0xFF333333), new Color(0xFFFF3333)));
        label.setText(error);
        mContentPanel.add(label);

        if (myBalloon == null)
            return;

        myBalloon.revalidate();
    }

}
