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

import com.gogh.plugin.adapter.TranslationManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

/**
 * Created by xiaofeng on 9/1/2016.
 */
/*public class ToolWindowAction extends BaseCodeInsightAction implements HintManagerImpl.ActionToIgnore, DumbAware, PopupAction {
    public ToolWindowAction() {
        setEnabledInModalContext(true);
        setInjectedContext(true);
    }

    @NotNull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return new CodeInsightActionHandler() {
            @Override
            public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
                TranslationManager.getInstance(project).showTranslation(editor);
            }
            @Override
            public boolean startInWriteAction() {
                return false;
            }
        };
    }

    @Override
    protected boolean isValidForLookup() {
        return true;
    }

    @Override
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        DataContext dataContext = event.getDataContext();

        presentation.setEnabled(false);

        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if(project != null){
            Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
            if(editor != null){
                SelectionModel selectionModel = editor.getSelectionModel();
                if(selectionModel.getSelectedText() != null){
                    presentation.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if(project != null && editor != null){
            actionPerformedImpl(project, editor);
        }
    }
}*/

public class ToolWindowAction extends AnAction implements DumbAware {

    public ToolWindowAction() {
        setEnabledInModalContext(true);
        setInjectedContext(true);
    }

    @Override
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        DataContext dataContext = event.getDataContext();

        presentation.setEnabled(false);

        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if(project != null){
            Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
            if(editor != null){
                SelectionModel selectionModel = editor.getSelectionModel();
                if(selectionModel.getSelectedText() != null){
                    presentation.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if(project != null && editor != null){
            TranslationManager.getInstance(project).showTranslation(editor);
        }
    }
}
