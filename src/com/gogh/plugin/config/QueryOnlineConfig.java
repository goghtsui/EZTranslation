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
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.AnimatedIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.List;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> An window to display the translation result.</p>
 * <p> Created by <b>高晓峰</b> on 8/21/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/21/2016 do create. </li>
 */
public class QueryOnlineConfig extends DialogWrapper implements IDialog {

    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 450;

    private static final JBColor MSG_FOREGROUND_ERROR = new JBColor(new Color(0xFF333333), new Color(0xFFFF2222));

    private static final Icon CLOSE_ICON = IconLoader.getIcon(ICON.ICON_CLOSE);
    private static final Icon CLOSE_PRESSED = IconLoader.getIcon(ICON.ICON_CLOSE_PRESSED);

    private static final Border BORDER_ACTIVE = new LineBorder(new JBColor(JBColor.GRAY, Gray._35));
    private static final Border BORDER_PASSIVE = new LineBorder(new JBColor(JBColor.LIGHT_GRAY, Gray._75));

    private JPanel titlePanel;
    private JPanel contentPane;
    private JButton queryBtn;
    private JLabel messageLabel;
    private JPanel msgPanel;
    private JTextPane resultText;
    private JScrollPane scrollPane;
    private JComboBox<String> queryComboBox;
    private JPanel textPanel;
    private JPanel processPanel;
    private AnimatedIcon processIcon;
    private CardLayout layout;

    private final IModel mModel;
    private final QueryOnlineAdapter mQueryOnlineAdapter;

    private String mLastQuery;
    private boolean mBroadcast;

    private boolean mLastMoveWasInsideDialog;
    private final AWTEventListener mAwtActivityListener = new AWTEventListener() {

        @Override
        public void eventDispatched(AWTEvent e) {
            if (e instanceof MouseEvent && e.getID() == MouseEvent.MOUSE_MOVED) {
                final boolean inside = isInside(new RelativePoint((MouseEvent) e));
                if (inside != mLastMoveWasInsideDialog) {
                    mLastMoveWasInsideDialog = inside;
                    ((ITitlePanel) titlePanel).myButton.repaint();
                }
            }
        }
    };

    public QueryOnlineConfig(@Nullable Project project) {
        super(project);
        setUndecorated(true);
        setModal(false);
        getPeer().setContentPane(createCenterPanel());

        mQueryOnlineAdapter = new QueryOnlineAdapter(this);
        mModel = new IModel(mQueryOnlineAdapter.getHistory());

        initViews();

        getRootPane().setOpaque(false);

        Toolkit.getDefaultToolkit().addAWTEventListener(mAwtActivityListener, AWTEvent.MOUSE_MOTION_EVENT_MASK);
        getWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(mAwtActivityListener);
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        contentPane.setPreferredSize(JBUI.size(MIN_WIDTH, MIN_HEIGHT));
        contentPane.setBorder(BORDER_ACTIVE);
        return contentPane;
    }

    private void createUIComponents() {
        final ITitlePanel panel = new ITitlePanel();
        panel.setText(IString.DIALOG_TITLE_NAME);
        panel.setActive(true);

        WindowMoveListener windowListener = new WindowMoveListener(panel);
        panel.addMouseListener(windowListener);
        panel.addMouseMotionListener(windowListener);

        getWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                panel.setActive(true);
                contentPane.setBorder(BORDER_ACTIVE);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                panel.setActive(false);
                contentPane.setBorder(BORDER_PASSIVE);
            }
        });

        titlePanel = panel;
        titlePanel.requestFocus();

        processIcon = new ProcessIcon();
    }

    private boolean isInside(@NotNull RelativePoint target) {
        Component cmp = target.getOriginalComponent();

        if (!cmp.isShowing()) return true;
        if (cmp instanceof MenuElement) return false;
        Window window = this.getWindow();
        if (UIUtil.isDescendingFrom(cmp, window)) return true;
        if (!isShowing()) return false;
        Point point = target.getScreenPoint();
        SwingUtilities.convertPointFromScreen(point, window);
        return window.contains(point);
    }

    private void initViews() {
        queryBtn.addActionListener(e -> onQueryButtonClick());
        getRootPane().setDefaultButton(queryBtn);

        initQueryComboBox();

        textPanel.setBorder(BORDER_ACTIVE);
        scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());

        JBColor background = new JBColor(new Color(0xFFFFFFFF), new Color(0xFF2B2B2B));
        messageLabel.setBackground(background);
        processPanel.setBackground(background);
        msgPanel.setBackground(background);
        resultText.setBackground(background);
        scrollPane.setBackground(background);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        layout = (CardLayout) textPanel.getLayout();
        layout.show(textPanel, "msg");

        setComponentPopupWindow();
    }

    private void onQueryButtonClick() {
        String query = resultText.getSelectedText();
        if (IString.isEmpty(query)) {
            query = queryComboBox.getEditor().getItem().toString();
        }
        query(query);
    }

    private void initQueryComboBox() {
        queryComboBox.setModel(mModel);

        final JTextField field = (JTextField) queryComboBox.getEditor().getEditorComponent();
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setSelectionStart(0);
                field.setSelectionEnd(field.getText().length());
            }
        });

        queryComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !mBroadcast) {
                onQuery();
            }
        });
        queryComboBox.setRenderer(new ListCellRendererWrapper<String>() {

            @Override
            public void customize(JList list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value != null && value.trim().length() > 20) {
                    String trim = value.trim();
                    setText(trim.substring(0, 15) + "..." + trim.substring(trim.length() - 3));
                } else {
                    setText(value == null ? "" : value.trim());
                }
            }
        });
    }

    private void setComponentPopupWindow() {
        JBPopupMenu menu = new JBPopupMenu();

        final JBMenuItem copy = new JBMenuItem("Copy", IconLoader.getIcon(ICON.ICON_COPY_DARK));
        copy.addActionListener(e -> {
            String selectedText = resultText.getSelectedText();
            if (!IString.isEmpty(selectedText)) {
                CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
                copyPasteManager.setContents(new StringSelection(selectedText));
            }
        });

        final JBMenuItem query = new JBMenuItem("Translate", IconLoader.getIcon(ICON.ICON_16));
        query.addActionListener(e -> query(resultText.getSelectedText()));

        menu.add(copy);
        menu.add(query);

        menu.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                boolean hasSelectedText = !IString.isEmpty(resultText.getSelectedText());
                copy.setEnabled(hasSelectedText);
                query.setEnabled(hasSelectedText);
            }
        });

        resultText.setComponentPopupMenu(menu);
    }

    public void show() {
        if (!isShowing()) {
            super.show();
        }

        if (mModel.getSize() > 0) {
            query(mModel.getElementAt(0));
        }
    }

    private void query(String query) {
        if (!IString.isEmpty(query)) {
            queryComboBox.getEditor().setItem(query);
            onQuery();
        }
    }

    private void onQuery() {
        String text = queryComboBox.getEditor().getItem().toString();
        if (!IString.isEmpty(text) && !text.equals(mLastQuery)) {
            resultText.setText("");
            processIcon.resume();
            layout.show(textPanel, "process");
            mQueryOnlineAdapter.query(text);
        }
    }

    @Override
    public void onUpdateHistory() {
        mBroadcast = true;
        mModel.fireContentsChanged();
        queryComboBox.setSelectedIndex(0);
        mBroadcast = false;
    }

    @Override
    public void onCreateView(@NotNull String query, @NotNull ResultEntity result) {
        mLastQuery = query;

        BalloonAdapter.insertQueryResultText(resultText.getDocument(), result);

        resultText.setCaretPosition(0);
        layout.show(textPanel, "result");
        processIcon.suspend();
    }

    @Override
    public void onError(@NotNull String error) {
        messageLabel.setText(error);
        messageLabel.setForeground(MSG_FOREGROUND_ERROR);
    }

    /**
     * 搜索框的属性配置
     */
    private static class IModel extends AbstractListModel<String> implements ComboBoxModel<String> {
        private final List<String> myFullList;
        private Object mySelectedItem;

        IModel(@NotNull List<String> list) {
            myFullList = list;
        }

        @Override
        public String getElementAt(int index) {
            return this.myFullList.get(index);
        }

        @Override
        public int getSize() {
            return myFullList.size();
        }

        @Override
        public Object getSelectedItem() {
            return this.mySelectedItem;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            this.mySelectedItem = anItem;
            this.fireContentsChanged();
        }

        void fireContentsChanged() {
            this.fireContentsChanged(this, -1, -1);
        }
    }

    /**
     * 窗口标题属性配置
     */
    private class ITitlePanel extends TitlePanel {

        final CloseButton myButton;

        ITitlePanel() {
            super();

            myButton = new CloseButton();
            add(myButton, BorderLayout.EAST);

            NonOpaquePanel panel = new NonOpaquePanel();
            panel.setPreferredSize(myButton.getPreferredSize());
            add(panel, BorderLayout.WEST);

            setActive(false);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            if (myButton != null) {
                myButton.setActive(active);
            }
        }
    }

    /**
     * 关闭按钮属性配置
     */
    private class CloseButton extends NonOpaquePanel {

        private boolean isPressedByMouse;
        private boolean isActive;

        CloseButton() {
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    QueryOnlineConfig.this.dispose();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressedByMouse = true;
                    CloseButton.this.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressedByMouse = false;
                    CloseButton.this.repaint();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isPressedByMouse = false;
                    CloseButton.this.repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(CLOSE_ICON.getIconWidth() + JBUI.scale(4), CLOSE_ICON.getIconHeight());
        }

        private void setActive(final boolean active) {
            this.isActive = active;
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (hasPaint()) {
                paintIcon(g, !isActive || isPressedByMouse ? CLOSE_PRESSED : CLOSE_ICON);
            }
        }

        private boolean hasPaint() {
            return getWidth() > 0 && mLastMoveWasInsideDialog;
        }

        private void paintIcon(@NotNull Graphics g, @NotNull Icon icon) {
            icon.paintIcon(this, g, JBUI.scale(2), (getHeight() - icon.getIconHeight()) / 2);
        }
    }
}