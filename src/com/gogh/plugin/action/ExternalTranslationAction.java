/*
 * Copyright 2016 Yuyou Chow
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

import com.gogh.plugin.common.IString;
import com.gogh.plugin.common.Translators;
import com.gogh.plugin.translator.Translator;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * modify by xiaofeng gogh on 2016/09/01
 */
public class ExternalTranslationAction extends AnAction {

	public ExternalTranslationAction() {
		setInjectedContext(true);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(!IString.isEmpty(getSelectedText(event)));
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

	@Override
	public void actionPerformed(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		final Project project = CommonDataKeys.PROJECT.getData(dataContext);
		final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

		if(project != null && editor != null){
			SelectionModel selectionModel = editor.getSelectionModel();
			String query = selectionModel.getSelectedText();
			if(query == null) return;

			showExternalTranslation(query, dataContext);
		}
	}

	public static void showExternalTranslation(String query, DataContext dataContext){
		final Component contextComponent = PlatformDataKeys.CONTEXT_COMPONENT.getData(dataContext);

		ApplicationManager.getApplication().executeOnPooledThread(() -> {
			final List<String> menuItems = new ArrayList<>();
			final List<String> urls = new ArrayList<>();
			final List<Icon> icons = new ArrayList<Icon>();

			for(Translator translator: Translators.getTranslator()){
				String url = translator.getExternalUrl(query);
				if(url != null){
					menuItems.add(translator.getMenuItem());
					urls.add(url);
					icons.add(translator.getIcon());
				}
			}

			ApplicationManager.getApplication().invokeLater( () -> JBPopupFactory.getInstance().createListPopup(
					new BaseListPopupStep<String>("Choose web platform"
                    , ArrayUtil.toStringArray(menuItems)
                    , ArrayUtil.toObjectArray(icons, Icon.class)) {
                @Override
                public PopupStep onChosen(final String selectedValue, final boolean finalChoice) {
                	if(selectedValue.equals(menuItems.get(0))){
						BrowserUtil.browse(urls.get(0));
					} else if(selectedValue.equals(menuItems.get(1))){
						BrowserUtil.browse(urls.get(1));
					}
                    return FINAL_CHOICE;
                }
            }).showInBestPositionFor(DataManager.getInstance().getDataContext(contextComponent))
					, ModalityState.NON_MODAL);
		});
	}

	public static void showExternalTranslation(String query, String externalUrl, DataContext dataContext){
		final Component contextComponent = PlatformDataKeys.CONTEXT_COMPONENT.getData(dataContext);

		ApplicationManager.getApplication().executeOnPooledThread(() -> {
			final List<String> urls = new ArrayList<String>();
			final List<Icon> icons = new ArrayList<Icon>();

			if(StringUtil.isEmptyOrSpaces(externalUrl)){
				for(Translator translator: Translators.getTranslator()){
					String url = translator.getExternalUrl(query);
					if(url != null){
						urls.add(url);
						icons.add(translator.getIcon());
					}
				}
			}else{
				urls.add(externalUrl);
			}

			ApplicationManager.getApplication().invokeLater( () -> {
				if(ContainerUtil.isEmpty(urls)){
					// do nothing
				}else if(urls.size() == 1){
					BrowserUtil.browse(urls.get(0));
				}else{
					JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<String>(
							"Choose web platform",
						ArrayUtil.toStringArray(urls), ArrayUtil.toObjectArray(icons, Icon.class)) {
						@Override
						public PopupStep onChosen(final String selectedValue, final boolean finalChoice) {
							BrowserUtil.browse(selectedValue);
							return FINAL_CHOICE;
						}
					}).showInBestPositionFor(DataManager.getInstance().getDataContext(contextComponent));
				}

			}, ModalityState.NON_MODAL);
		});
	}


}
