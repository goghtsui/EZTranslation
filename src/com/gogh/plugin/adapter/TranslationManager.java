package com.gogh.plugin.adapter;

import com.gogh.plugin.common.IBundle;
import com.gogh.plugin.common.IString;
import com.gogh.plugin.common.Translators;
import com.gogh.plugin.entity.ResultEntity;
import com.gogh.plugin.translator.Translator;
import com.gogh.plugin.translator.YoudaoTranslator;
import com.gogh.plugin.ui.TranslationComponent;
import com.intellij.codeInsight.documentation.DockablePopupManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.BaseNavigateToSourceAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ScrollingUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.ui.popup.PopupPositionManager;
import com.intellij.util.Alarm;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

/**
 * Created by xiaofeng on 9/1/2016.
 */
public class TranslationManager extends DockablePopupManager<TranslationComponent> {

    public static final DataKey<String> SELECTED_QUICK_TRANSLATION_TEXT = DataKey.create("QUICK_TRANSLATION.SELECTED_TEXT");

    private static final Logger LOG = Logger.getInstance("#" + TranslationManager.class.getName());

    private Editor myEditor;
    private boolean myCloseOnSneeze;
    private ActionCallback myLastAction;
    private AnAction myRestorePopupAction;
    private Component myPreviouslyFocused;
    private final Alarm myUpdateTranslationAlarm;
    private WeakReference<JBPopup> myTranslationHintRef;
    private TranslationComponent myTestTranslationComponent;

    private final ActionManager myActionManager;

    public TranslationManager(final Project project, ActionManager manager) {
        super(project);

        myActionManager = manager;
        final AnActionListener actionListener = new AnActionListener() {
            @Override
            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                final JBPopup hint = getTranslationHint();
                if (hint != null) {
                    if (action instanceof HintManagerImpl.ActionToIgnore) {
                        ((AbstractPopup) hint).focusPreferredComponent();
                        return;
                    }
                    if (action instanceof ScrollingUtil.ListScrollAction) return;
                    if (action == myActionManager.getAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)) return;
                    if (action == myActionManager.getAction(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)) return;
                    if (action == myActionManager.getAction(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN)) return;
                    if (action == myActionManager.getAction(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP)) return;
                    if (IString.TRANSLATION_INPLACE_SETTINGS.equals(event.getPlace())) return;
                    if (action instanceof BaseNavigateToSourceAction) return;
                    closeTranslationHint();
                }
            }

            @Override
            public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
            }

            @Override
            public void beforeEditorTyping(char c, DataContext dataContext) {
                final JBPopup hint = getTranslationHint();
                if (hint != null) {
                    hint.cancel();
                }
            }
        };

        myActionManager.addAnActionListener(actionListener, project);
        myUpdateTranslationAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, myProject);
    }

    public static TranslationManager getInstance(Project project) {
        return ServiceManager.getService(project, TranslationManager.class);
    }

    @Override
    protected String getShowInToolWindowProperty() {
        return IString.SHOW_TRANSLATION_IN_TOOL_WINDOW;
    }

    @Override
    protected String getAutoUpdateEnabledProperty() {
        return IString.TRANSLATION_AUTO_UPDATE_ENABLED;
    }

    @Override
    protected String getAutoUpdateTitle() {
        return IString.AUTO_UPDATE;
    }

    @Override
    protected String getRestorePopupDescription() {
        return IString.RESTORE_VIEW;
    }

    @Override
    protected String getAutoUpdateDescription() {
        return IString.REFRESH_AUTOMATICALLY;
    }

    @NotNull
    @Override
    protected AnAction createRestorePopupAction() {
        myRestorePopupAction = super.createRestorePopupAction();
        return myRestorePopupAction;
    }

    @Override
    public void restorePopupBehavior() {
        if (myPreviouslyFocused != null) {
            IdeFocusManager.getInstance(myProject).requestFocus(myPreviouslyFocused, true);
        }
        super.restorePopupBehavior();
        updateComponent();
    }

    @Override
    public void createToolWindow(PsiElement element, PsiElement originalElement) {
        super.createToolWindow(element, originalElement);

        // switch between toolWindow and popup
        if (myToolWindow != null) {
            myToolWindow.getComponent().putClientProperty(ChooseByNameBase.TEMPORARILY_FOCUSABLE_COMPONENT_KEY, Boolean.TRUE);
            if (myRestorePopupAction != null) {
                ShortcutSet quickTranslateShortCut = ActionManager.getInstance()
                        .getAction(IString.ACTION_QUICK_TRANSLATE).getShortcutSet();
                myRestorePopupAction.registerCustomShortcutSet(quickTranslateShortCut, myToolWindow.getComponent());
                myRestorePopupAction = null;
            }
        }
    }

    @Override
    protected TranslationComponent createComponent() {
        return new TranslationComponent(this, createActions());
    }

    @Override
    protected void doUpdateComponent(PsiElement element, PsiElement originalElement, TranslationComponent component) {
        fetchTranslation(getDefaultCollector(myEditor.getSelectionModel().getSelectedText()), component);
    }

    @Override
    protected void doUpdateComponent(Editor editor, PsiFile psiFile) {
        showTranslation(editor, false, null);
    }

    @Override
    protected void doUpdateComponent(@NotNull PsiElement element) {
//		System.out.println("doUpdateComponent(@NotNull PsiElement element)");
    }

    @Override
    protected String getTitle(PsiElement element) {
        return getTitle(element, true);
    }

    static String getTitle(@Nullable final PsiElement element, final boolean _short) {
        return _short ? "" : IString.TOOL_WINDOW_ID;
    }

    @Override
    protected String getToolwindowId() {
        return IString.TOOL_WINDOW_ID;
    }

    @Nullable
    public JBPopup getTranslationHint() {
        if (myTranslationHintRef == null) return null;
        JBPopup hint = myTranslationHintRef.get();
        if (hint == null || !hint.isVisible() && !ApplicationManager.getApplication().isUnitTestMode()) {
            myTranslationHintRef = null;
            return null;
        }
        return hint;
    }

    private void closeTranslationHint() {
        JBPopup hint = getTranslationHint();
        if (hint == null) {
            return;
        }
        myCloseOnSneeze = false;
        hint.cancel();
        Component toFocus = myPreviouslyFocused;
        hint.cancel();
        if (toFocus != null) {
            IdeFocusManager.getInstance(myProject).requestFocus(toFocus, true);
        }
    }

    public void showTranslation(@NotNull Editor editor) {
        showTranslation(editor, true, null);
    }

    public void showTranslation(@NotNull Editor editor, boolean requestFocus, @Nullable final Runnable closeCallback) {
        myEditor = editor;

        SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.getSelectedText() != null) {
            doShowTranslation(selectionModel.getSelectedText(), requestFocus, closeCallback);
        }
    }

    private void doShowTranslation(@NotNull String queryText, boolean requestFocus, @Nullable final Runnable closeCallback) {
        final Project project = myProject;
        if (!project.isOpen()) return;

        myPreviouslyFocused = WindowManagerEx.getInstanceEx().getFocusedComponent(project);

        JBPopup _oldHint = getTranslationHint();

        if (myToolWindow == null && PropertiesComponent.getInstance().isTrueValue(IString.SHOW_TRANSLATION_IN_TOOL_WINDOW)) {
            createToolWindow(null, null);
        } else if (myToolWindow != null) {
            Content content = myToolWindow.getContentManager().getSelectedContent();
            if (content != null) {
                TranslationComponent component = (TranslationComponent) content.getComponent();
                boolean samQuery = queryText.equals(component.getQuery());
                if (samQuery) {
                    JComponent preferredFocusableComponent = content.getPreferredFocusableComponent();
                    // focus toolwindow on the second actionPerformed
                    boolean focus = requestFocus || CommandProcessor.getInstance().getCurrentCommand() != null;
                    if (preferredFocusableComponent != null && focus) {
                        IdeFocusManager.getInstance(myProject).requestFocus(preferredFocusableComponent, true);
                    }
                } else {
                    content.setDisplayName(getTitle(null, true));
                    fetchTranslation(getDefaultCollector(queryText), component, true);
                }
            }
            if (!myToolWindow.isVisible()) {

                myToolWindow.show(null);
            }
        } else if (_oldHint != null && _oldHint.isVisible() && _oldHint instanceof AbstractPopup) {
            TranslationComponent oldComponent = (TranslationComponent) ((AbstractPopup) _oldHint).getComponent();
            fetchTranslation(getDefaultCollector(queryText), oldComponent);
        } else {
            showInPopup(queryText, requestFocus, closeCallback);
        }
    }

    private void showInPopup(@NotNull String queryText, boolean requestFocus, @Nullable final Runnable closeCallback) {
        final TranslationComponent component = myTestTranslationComponent == null ?
                new TranslationComponent(this) : myTestTranslationComponent;

        // todo NavigateCallback

        Processor<JBPopup> pinCallback = popup -> {
            createToolWindow(null, null);
            myToolWindow.setAutoHide(false);
            popup.cancel();
            return false;
        };

        ActionListener actionListener = e -> {
            createToolWindow(null, null);
            final JBPopup hint = getTranslationHint();
            if (hint != null && hint.isVisible()) hint.cancel();
        };

        java.util.List<Pair<ActionListener, KeyStroke>> actions = ContainerUtil.newSmartList();
        AnAction quickDocAction = ActionManager.getInstance().getAction(IString.ACTION_QUICK_TRANSLATE);
        for (Shortcut shortcut : quickDocAction.getShortcutSet().getShortcuts()) {
            if (!(shortcut instanceof KeyboardShortcut)) continue;
            actions.add(Pair.create(actionListener, ((KeyboardShortcut) shortcut).getFirstKeyStroke()));
        }

        final JBPopup hint = JBPopupFactory.getInstance().createComponentPopupBuilder(component, component)
                .setProject(myProject)
                .setKeyboardActions(actions)
                .setDimensionServiceKey(myProject, IString.TRANSLATION_LOCATION_AND_SIZE, false)
                .setResizable(true)
                .setMovable(true)
                .setRequestFocus(requestFocus)
                .setCancelOnClickOutside(true)
                .setTitle(getTitle(null, false))
                .setCouldPin(pinCallback)
                .setModalContext(false)
                .setCancelCallback(() -> {
                    myCloseOnSneeze = false;
                    if (closeCallback != null) {
                        closeCallback.run();
                    }
                    Disposer.dispose(component);
                    myEditor = null;
                    myPreviouslyFocused = null;
                    return Boolean.TRUE;
                })
                .setKeyEventHandler(e -> {
                    if (myCloseOnSneeze) {
                        closeTranslationHint();
                    }
                    if (AbstractPopup.isCloseRequest(e) && getTranslationHint() != null) {
                        closeTranslationHint();
                        return true;
                    }
                    return false;
                })
                .createPopup();

        component.setHint(hint);

        fetchTranslation(getDefaultCollector(queryText), component);

        myTranslationHintRef = new WeakReference<>(hint);
    }

    public void fetchTranslation(final TranslationCollector provider, final TranslationComponent component) {
        doFetchTranslation(component, provider, true, false);
    }

    public void fetchTranslation(final TranslationCollector provider, final TranslationComponent component, final boolean clearHistory) {
        doFetchTranslation(component, provider, true, clearHistory);
    }

    private ActionCallback doFetchTranslation(final TranslationComponent component, final TranslationCollector provider
            , final boolean cancelRequests, final boolean clearHistory) {
        final ActionCallback callback = new ActionCallback();
        myLastAction = callback;

        boolean wasEmpty = component.isEmpty();
        component.startWait();
        if (cancelRequests) {
            myUpdateTranslationAlarm.cancelAllRequests();
        }
        if (wasEmpty) {
            component.setText(IBundle.message("translation.fetching.progress"), null, null);
            final AbstractPopup jbPopup = (AbstractPopup) getTranslationHint();
            if (jbPopup != null) {
                jbPopup.setDimensionServiceKey(null);
            }
        }

        myUpdateTranslationAlarm.addRequest(() -> {
            if (myProject.isDisposed()) return;
            LOG.debug("Started fetching com.intellij.translation...");
            final Throwable[] ex = new Throwable[1];
            String text = null;
            try {
                text = provider.getTranslation();
            } catch (Throwable e) {
                LOG.info(e);
                ex[0] = e;
            }
            if (ex[0] != null) {
                //noinspection SSBasedInspection
                SwingUtilities.invokeLater(() -> {
                    String message = ex[0] instanceof IndexNotReadyException
                            ? "Translation is not available until indices are built."
                            : IBundle.message("translation.external.fetch.error.message");
                    component.setText(message, null, null);
                    callback.setDone();
                });
                return;
            }

            LOG.debug("Translation fetched successfully:\n", text);

            final String translationText = text;

            //noinspection SSBasedInspection
            SwingUtilities.invokeLater(() -> {
                PsiDocumentManager.getInstance(myProject).commitAllDocuments();

                if (translationText == null) {
                    component.setText(IBundle.message("translation.no.info.found"), provider.getQuery(), null);
                } else if (translationText.isEmpty()) {
                    component.setText(component.getText(), provider.getQuery(), null);
                } else {
//                    component.setData(provider.getQuery(), translationText, provider.getTranslator());
                    try {
                        component.setDocumentData(provider.getQuery(), translationText
                                , provider.getDocTranslation(), provider.getTranslator());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                final AbstractPopup jbPopup = (AbstractPopup) getTranslationHint();
                if (jbPopup == null) {
                    callback.setDone();
                    return;
                }

                jbPopup.setDimensionServiceKey(IString.TRANSLATION_LOCATION_AND_SIZE);
                jbPopup.setCaption(getTitle(null, false));
                callback.setDone();
            });
        }, 10);

        return callback;
    }

    private TranslationCollector getDefaultCollector(final String queryText) {
        return new DefaultTranslationCollector(queryText);
    }

    private interface TranslationCollector {
        @Nullable
        String getTranslation() throws Exception;

        @Nullable
        ResultEntity getDocTranslation() throws Exception;

        @Nullable
        String getQuery();

        @Nullable
        String getExternalUrl();

        @Nullable
        Translator getTranslator();
    }

    private class DefaultTranslationCollector implements TranslationCollector {
        private final String myQuery;
        private String myExternalUrl;
        private Translator myTranslator;

        public DefaultTranslationCollector(String query) {
            this.myQuery = query;
        }

        @Nullable
        @Override
        public String getQuery() {
            return myQuery;
        }

        @Nullable
        @Override
        public String getExternalUrl() {
            return myExternalUrl;
        }

        @Nullable
        @Override
        public Translator getTranslator() {
            return myTranslator;
        }

        @Nullable
        @Override
        public String getTranslation() throws Exception {
            if (myQuery != null) {
                for (Translator provider : Translators.getTranslator()) {
                    final String translation = provider.fetchInfo(myQuery);
                    if (translation != null) {
                        LOG.debug("Fetched translation from ", provider.getTitle());
                        myExternalUrl = provider.getExternalUrl(myQuery);
                        myTranslator = provider;
                        return translation;
                    }
                }
            }
            return null;
        }

        @Nullable
        @Override
        public ResultEntity getDocTranslation() throws Exception {
            if (myQuery != null) {
                for (Translator provider : Translators.getTranslator()) {
                    myExternalUrl = provider.getExternalUrl(myQuery);
                    myTranslator = provider;
                    if (provider instanceof YoudaoTranslator) {
                        final ResultEntity translation = ((YoudaoTranslator) provider).fetchDocInfo(myQuery);
                        if (translation != null) {
                            LOG.debug("Fetched translation from ", provider.getTitle());
                            return translation;
                        }
                    }
                }
            }
            return null;
        }
    }

    public void showHint(final JBPopup hint) {
        final Component focusOwner = IdeFocusManager.getInstance(myProject).getFocusOwner();
        DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
        PopupPositionManager.positionPopupInBestPosition(hint, myEditor, dataContext);
    }

    public Project getProject() {
        return myProject;
    }
}
