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
import com.gogh.plugin.entity.OnlineEntity;
import com.gogh.plugin.entity.PhoneticEntity;
import com.gogh.plugin.entity.ResultEntity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public final class BalloonAdapter {

    @SuppressWarnings("SpellCheckingInspection")
    private static final Logger LOG = Logger.getInstance("#BalloonAdapter");

    private static final SimpleAttributeSet ATTR_QUERY = new SimpleAttributeSet();
    private static final SimpleAttributeSet ATTR_EXPLAIN = new SimpleAttributeSet();
    private static final SimpleAttributeSet ATTR_PRE_EXPLAINS = new SimpleAttributeSet();
    private static final SimpleAttributeSet ATTR_EXPLAINS = new SimpleAttributeSet();
    private static final SimpleAttributeSet ATTR_WEB_EXPLAIN_TITLE = new SimpleAttributeSet();
    private static final SimpleAttributeSet ATTR_WEB_EXPLAIN_KEY = new SimpleAttributeSet();
    private static final SimpleAttributeSet ATTR_WEB_EXPLAIN_VALUES = new SimpleAttributeSet();

    static {
        StyleConstants.setItalic(ATTR_QUERY, true);
        StyleConstants.setBold(ATTR_QUERY, true);
        StyleConstants.setFontSize(ATTR_QUERY, JBUI.scaleFontSize(19));
        // the word
        StyleConstants.setForeground(ATTR_QUERY, new JBColor(0xFF333333, 0xFFCC7832));

        // pronunciation
        StyleConstants.setForeground(ATTR_EXPLAIN, new JBColor(0xFF333333, 0xFF8CBCE1));

        // type name
        StyleConstants.setItalic(ATTR_PRE_EXPLAINS, true);
        StyleConstants.setForeground(ATTR_PRE_EXPLAINS, new JBColor(0xFF333333, 0xFFDCDCDC));
        StyleConstants.setFontSize(ATTR_PRE_EXPLAINS, JBUI.scaleFontSize(16));

        // type content
        StyleConstants.setForeground(ATTR_EXPLAINS, new JBColor(0xFF333333, 0xFFFFC66D));
        StyleConstants.setFontSize(ATTR_PRE_EXPLAINS, JBUI.scaleFontSize(16));

        // explain title
        StyleConstants.setForeground(ATTR_WEB_EXPLAIN_TITLE, new JBColor(0xFF333333, 0xFF808080));
        // explain content
        StyleConstants.setForeground(ATTR_WEB_EXPLAIN_KEY, new JBColor(0xFF333333, 0xFF708090));
        StyleConstants.setForeground(ATTR_WEB_EXPLAIN_VALUES, new JBColor(0xFF333333, 0xFFE6E6FA));
    }

    private BalloonAdapter() {
        LOG.debug("BalloonAdapter constructor.");
    }

    public static String getErrorMessage(ResultEntity result) {
        if (result == null)
            return IString.ERROR_FAILED;

        if (result.getErrorCode() == ResultEntity.ERROR_CODE_NONE)
            return null;

        String message;
        switch (result.getErrorCode()) {
            case ResultEntity.ERROR_CODE_RESTRICTED:
                message = IString.ERROR_RESTRICTED;
                break;
            case ResultEntity.ERROR_CODE_QUERY_TOO_LONG:
                message = IString.ERROR_QUERY_TOO_LONG;
                break;
            case ResultEntity.ERROR_CODE_UNSUPPORTED_LANG:
                message = IString.ERROR_UNSUPPORTED_LANG;
                break;
            case ResultEntity.ERROR_CODE_INVALID_KEY:
                message = IString.ERROR_INVALID_KEY;
                break;
            case ResultEntity.ERROR_CODE_FAIL:
            case ResultEntity.ERROR_CODE_NO_RESULT:
            default:
                message = IString.ERROR_FAILED;
                break;
        }

        return message;
    }

    public static void insertQueryResultText(@NotNull Document document, @NotNull ResultEntity result) {
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return;
        }

        insertHeader(document, result);

        PhoneticEntity phoneticEntity = result.getPhoneticEntity();
        if (phoneticEntity != null) {
            insertExplain(document, phoneticEntity.getExplains());
        } else {
            insertExplain(document, result.getTranslation());
        }

        OnlineEntity[] onlineEntities = result.getOnlineEntities();
        insertWebExplain(document, onlineEntities);

        if (document.getLength() < 1)
            return;

        try {
            int offset = document.getLength() - 1;
            String text = document.getText(offset, 1);
            if (text.charAt(0) == '\n') {
                document.remove(offset, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertHeader(Document document, ResultEntity result) {
        String query = result.getQuery();

        try {
            if (!IString.isEmpty(query)) {
                query = query.trim();
                document.insertString(document.getLength(),
                        Character.toUpperCase(query.charAt(0)) + query.substring(1) + "\n", ATTR_QUERY);
            }

            PhoneticEntity be = result.getPhoneticEntity();
            if (be != null) {
                StringBuilder explain = new StringBuilder();

                String pho = be.getPhonetic();
                if (!IString.isEmpty(pho)) {
                    explain.append("[");
                    explain.append(pho);
                    explain.append("]  ");
                }

                pho = be.getPhoneticUK();
                if (!IString.isEmpty(pho)) {
                    explain.append("英[");
                    explain.append(pho);
                    explain.append("]  ");
                }

                pho = be.getPhoneticUS();
                if (!IString.isEmpty(pho)) {
                    explain.append("美[");
                    explain.append(pho);
                    explain.append("]");
                }

                if (explain.length() > 0) {
                    document.insertString(document.getLength(), explain.toString() + "\n", ATTR_EXPLAIN);
                }
            }

            document.insertString(document.getLength(), "\n", null);
        } catch (BadLocationException e) {
            LOG.error("insertHeader ", e);
        }
    }

    private static void insertExplain(Document doc, String[] explains) {
        if (explains == null || explains.length == 0)
            return;

        try {
            for (String exp : explains) {
                if (IString.isEmpty(exp))
                    continue;

                int i = exp.indexOf('.');
                if (i > 0) {
                    doc.insertString(doc.getLength(), exp.substring(0, i + 1), ATTR_PRE_EXPLAINS);
                    exp = exp.substring(i + 1);
                }

                doc.insertString(doc.getLength(), exp + '\n', ATTR_EXPLAINS);
            }

            doc.insertString(doc.getLength(), "\n", null);
        } catch (BadLocationException e) {
            LOG.error("insertExplain ", e);
        }
    }

    /**
     *
     * @param doc
     * @param onlineEntities
     */
    private static void insertWebExplain(Document doc, OnlineEntity[] onlineEntities) {
        if (onlineEntities == null || onlineEntities.length == 0)
            return;

        try {
            doc.insertString(doc.getLength(), "网络释义:\n", ATTR_WEB_EXPLAIN_TITLE);

            for (OnlineEntity onlineEntity : onlineEntities) {
                doc.insertString(doc.getLength(), onlineEntity.getKey(), ATTR_WEB_EXPLAIN_KEY);
                doc.insertString(doc.getLength(), " -", null);


                String[] values = onlineEntity.getValues();
                for (int i = 0; i < values.length; i++) {
                    doc.insertString(doc.getLength(), " " + values[i] + (i < values.length - 1 ? ";" : ""),
                            ATTR_WEB_EXPLAIN_VALUES);
                }
                doc.insertString(doc.getLength(), "\n", null);
            }

        } catch (BadLocationException e) {
            LOG.error("insertWebExplain ", e);
        }
    }

}
