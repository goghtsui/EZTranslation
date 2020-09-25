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
package com.gogh.plugin.common;

import org.jetbrains.annotations.NonNls;

/**
 * Created by xiaofeng on 8/23/2016.
 */
public class IString {

    // TranslationManager
    @NonNls
    public static final String TRANSLATION_LOCATION_AND_SIZE = "com.intellij.translation.popup";
    public static final String SHOW_TRANSLATION_IN_TOOL_WINDOW = "ShowTranslationInToolWindow";
    public static final String TRANSLATION_AUTO_UPDATE_ENABLED = "TranslationAutoUpdateEnabled";

    public static final String AUTO_UPDATE = "Auto-update";
    public static final String RESTORE_VIEW = "Restore popup view mode";
    public static final String REFRESH_AUTOMATICALLY = "Refresh translation automatically";

    public static final String TOOL_WINDOW_ID = "Translation";
    public static final String ACTION_QUICK_TRANSLATE = "QuickTranslate";

    // TranslationCompoment
    public static final String ACTION_EXTERNAL_TRANSLATION = "ExternalTranslation";
    public static final String TRANSLATION_INPLACE_SETTINGS = "TranslationInplaceSettings";

    // TBalloonConfig
    public static final String LOADING = "Loading...";

    // Settings
    public static final String URL = "http://fanyi.youdao.com/openapi?path=data-mode";

    public static final String SETTING_NAME = "Easy-Translation";

    public static final String API_KEY_NAME = "TranslationPlugin.API_KEY_NAME";
    public static final String API_KEY_VALUE = "TranslationPlugin.API_KEY_VALUE";
    public static final String API_KEY_USER_DEFAULT = "TranslationPlugin.API_KEY_USER_DEFAULT";

    //TranslationRequest
    @SuppressWarnings("SpellCheckingInspection")
    public static final String BASIC_URL = "http://fanyi.youdao.com/openapi.do?type=data&doctype=json&version=1.1&keyfrom=";

    // GoogleTranslator
    public static final String GOOGLE_TRANSLATION =  "Google Translation";
    public static final String GOOGLE_TRANSLATION_URL =  "https://translate.google.cn/#auto/zh-CN/";

    // QueryOnlineConfig
    public static final String DIALOG_TITLE_NAME = "QueryOnline";

    // QueryOnlineAction
    public static final String QUERY_ONLINE_ACTION_NAME = "QueryOnlineAction";

    // BalloonAction
    public static final String BALLOON_ACTION_NAME = "EZTranslate";
    public static final String TRANSLATE_NAME = "Translate";

    public static final String PANEL_BACKGROUND = "Panel.background";
    public static final String JBUI_FONT_YAHEI = "Microsoft YaHei";

    public static final String ERROR_RESTRICTED = "请求过于频繁，请尝试更换API KEY";
    public static final String ERROR_QUERY_TOO_LONG = "Query content too long";
    public static final String ERROR_UNSUPPORTED_LANG = "Unsupported lang";
    public static final String ERROR_INVALID_KEY = "无效的API KEY";
    public static final String ERROR_FAILED = "Nothing to show";

    public static boolean isEmpty(String str) {
        return null == str || str.trim().length() == 0;
    }

    /**
     * 单词拆分
     */
    public static String splitWord(String input) {
        if (isEmpty(input))
            return input;

        return input.replace("_", " ")
                .replaceAll("([A-Z][a-z]+)|([0-9\\W]+)", " $0 ")
                .replaceAll("[A-Z]{2,}", " $0")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

}
