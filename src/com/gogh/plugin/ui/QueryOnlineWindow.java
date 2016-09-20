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
package com.gogh.plugin.ui;

import com.gogh.plugin.config.QueryOnlineConfig;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Copyright (c) 2016 All rights reserved by 高晓峰（xiaofeng355@gmail.com）
 * <p> An manager to manage the popup window, display、refresh UI.</p>
 * <p> Created by <b>高晓峰</b> on 8/121/2016. </p>
 * <p> ChangeLog: </p>
 * <li> 高晓峰 on 8/21/2016 do create. </li>
 */
@SuppressWarnings("WeakerAccess")
public class QueryOnlineWindow {

    private static final Logger LOG = Logger.getInstance("#QueryOnlineWindow");

    /**
     * the instance of this class.
     */
    private final static QueryOnlineWindow MANAGER = new QueryOnlineWindow();

    private QueryOnlineConfig myShowingDialog;

    /**
     * constructor
     */
    private QueryOnlineWindow() {
        LOG.debug("QueryOnlineWindow constructor.");
    }

    /**
     * create an instance of this class.
     * @return the instance of this class
     */
    public static QueryOnlineWindow getInstance() {
        return MANAGER;
    }

    /**
     *
     * @param project
     */
    public void show(@Nullable Project project) {
        if (myShowingDialog == null) {
            myShowingDialog = new QueryOnlineConfig(project);
            myShowingDialog.getWindow().addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
//                    myShowingDialog = null;
                }
            });
        }
        myShowingDialog.show();
    }

}
