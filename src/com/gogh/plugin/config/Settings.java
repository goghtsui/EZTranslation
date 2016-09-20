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

import com.gogh.plugin.common.IString;
import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * 设置
 */
@SuppressWarnings("WeakerAccess")
public class Settings implements Configurable, ItemListener {

    private static final boolean DEFAULT_USER_DEFAULT_KEY = true;

    private JPanel contentPanel;
    @SuppressWarnings("unused")
    private LinkLabel linkLabel;
    private JTextField keyNameField;
    private JTextField keyValueField;
    private JCheckBox checkBox;

    @Nls
    @Override
    public String getDisplayName() {
        return IString.SETTING_NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        checkBox.addItemListener(this);
        return contentPanel;
    }

    private void createUIComponents() {
        linkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                obtainApiKey();
            }
        });
    }

    private static void obtainApiKey() {
        WebBrowser browser = WebBrowserManager.getInstance().getFirstActiveBrowser();
        if (browser != null) {
            BrowserLauncher.getInstance().browseUsingPath(IString.URL, null, browser, null
                    , ArrayUtil.EMPTY_STRING_ARRAY);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        switchKey();
    }

    private void switchKey() {
        if (checkBox.isSelected()) {
            setupDefaultKey();
        } else {
            setupCustomKey();
        }
    }

    private void setupDefaultKey() {
        if (IString.isEmpty(keyNameField.getText())
                && IString.isEmpty(keyValueField.getText())) {
            PropertiesComponent component = PropertiesComponent.getInstance();
            component.setValue(IString.API_KEY_NAME, null);
            component.setValue(IString.API_KEY_VALUE, null);
        }

        keyNameField.setText("Default");
        keyNameField.setEnabled(false);
        keyValueField.setText("Default");
        keyValueField.setEnabled(false);
    }

    private void setupCustomKey() {
        PropertiesComponent component = PropertiesComponent.getInstance();

        keyNameField.setText(component.getValue(IString.API_KEY_NAME, ""));
        keyNameField.setEnabled(true);
        keyValueField.setText(component.getValue(IString.API_KEY_VALUE, ""));
        keyValueField.setEnabled(true);
    }

    @Override
    public boolean isModified() {
        return !IString.isEmpty(keyNameField.getText())
                && !IString.isEmpty(keyValueField.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent component = PropertiesComponent.getInstance();

        boolean validKey = !IString.isEmpty(keyNameField.getText())
                && !IString.isEmpty(keyValueField.getText());
        boolean useDefault = checkBox.isSelected();
        if (!useDefault) {
            component.setValue(IString.API_KEY_NAME, keyNameField.getText());
            component.setValue(IString.API_KEY_VALUE, keyValueField.getText());
        }

        component.setValue(IString.API_KEY_USER_DEFAULT, useDefault || !validKey, DEFAULT_USER_DEFAULT_KEY);
    }

    @Override
    public void reset() {
        checkBox.setSelected(isUseDefaultKey());
    }

    @Override
    public void disposeUIResources() {
        checkBox.removeItemListener(this);
    }

    public static boolean isUseDefaultKey() {
        return PropertiesComponent.getInstance().getBoolean(IString.API_KEY_USER_DEFAULT, DEFAULT_USER_DEFAULT_KEY);
    }

    public static String getApiKeyName() {
        return PropertiesComponent.getInstance().getValue(IString.API_KEY_NAME, "");
    }

    public static String getApiKeyValue() {
        return PropertiesComponent.getInstance().getValue(IString.API_KEY_VALUE, "");
    }

}