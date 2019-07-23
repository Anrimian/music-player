package com.github.anrimian.musicplayer.ui.editor;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

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
    void showEnterAuthorDialog(Composition composition);

    @StateStrategyType(SkipStrategy.class)
    void showEnterTitleDialog(Composition composition);

    @StateStrategyType(SkipStrategy.class)
    void showEnterFileNameDialog(Composition composition);
}
