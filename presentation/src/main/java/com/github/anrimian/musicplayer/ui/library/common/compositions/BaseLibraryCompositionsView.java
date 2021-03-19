package com.github.anrimian.musicplayer.ui.library.common.compositions;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.mvp.ListMvpView;

import java.util.Collection;
import java.util.List;

import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;
import moxy.viewstate.strategy.alias.Skip;

public interface BaseLibraryCompositionsView extends ListMvpView<Composition> {

    @OneExecution
    void showSelectPlayListDialog();

    @OneExecution
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @OneExecution
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @OneExecution
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @OneExecution
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @OneExecution
    void showDeleteCompositionMessage(List<Composition> compositionsToDelete);

    @Skip
    void onCompositionSelected(Composition composition, int position);

    @Skip
    void onCompositionUnselected(Composition composition, int position);

    @Skip
    void setItemsSelected(boolean selected);

    @AddToEndSingle
    void showSelectionMode(int count);

    @Skip
    void shareCompositions(Collection<Composition> selectedCompositions);

    @Skip
    void showCompositionActionDialog(Composition composition, int position);

    @OneExecution
    void showErrorMessage(ErrorCommand errorCommand);

    @AddToEndSingle
    void setDisplayCoversEnabled(boolean isCoversEnabled);

    @OneExecution
    void onCompositionsAddedToPlayNext(List<Composition> compositions);

    @OneExecution
    void onCompositionsAddedToQueue(List<Composition> compositions);

    @AddToEndSingle
    void showCurrentComposition(CurrentComposition currentComposition);

    @OneExecution
    void restoreListPosition(ListPosition listPosition);
}
