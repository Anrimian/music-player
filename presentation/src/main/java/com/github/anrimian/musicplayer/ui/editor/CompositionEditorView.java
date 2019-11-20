package com.github.anrimian.musicplayer.ui.editor;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface CompositionEditorView extends MvpView {

    String DISPLAY_COMPOSITION_STATE = "display_composition_state";

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showCompositionLoadingError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showComposition(Composition composition);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(SkipStrategy.class)
    void showEnterAuthorDialog(Composition composition, String[] hints);

    @StateStrategyType(SkipStrategy.class)
    void showEnterTitleDialog(Composition composition);

    @StateStrategyType(SkipStrategy.class)
    void showEnterFileNameDialog(Composition composition);

    @StateStrategyType(SkipStrategy.class)
    void copyFileNameText(String filePath);
}
