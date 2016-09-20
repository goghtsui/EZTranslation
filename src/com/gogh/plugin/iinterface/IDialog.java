package com.gogh.plugin.iinterface;

import com.gogh.plugin.entity.ResultEntity;
import org.jetbrains.annotations.NotNull;

public interface IDialog {

    /**
     * update history record of words list.
     */
    void onUpdateHistory();

    /**
     * display translation result online.
     * @param word the word you want to translate.
     * @param result an entity from translation text.
     */
    void onCreateView(@NotNull String word, @NotNull ResultEntity result);

    /**
     * display error message.
     * @param error error message.
     */
    void onError(@NotNull String error);

}
