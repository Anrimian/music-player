package com.github.anrimian.musicplayer.ui.editor.composition

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy
import moxy.MvpView
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val DISPLAY_COMPOSITION_STATE = "display_composition_state"
private const val CHANGE_FILE_STATE = "change_file_state"

interface CompositionEditorView : MvpView {

    @OneExecution
    fun closeScreen()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = DISPLAY_COMPOSITION_STATE)
    fun showCompositionLoadingError(errorCommand: ErrorCommand)

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = DISPLAY_COMPOSITION_STATE)
    fun showComposition(composition: FullComposition)

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = CHANGE_FILE_STATE)
    fun showChangeFileProgress()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = CHANGE_FILE_STATE)
    fun hideChangeFileProgress()

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun showCheckTagsErrorMessage(errorCommand: ErrorCommand)

    @Skip
    fun showEnterAuthorDialog(composition: FullComposition, hints: Array<String>?)

    @Skip
    fun showEnterTitleDialog(composition: FullComposition)

    @Skip
    fun showEnterFileNameDialog(composition: FullComposition)

    @Skip
    fun copyFileNameText(filePath: String)

    @Skip
    fun showEnterAlbumDialog(composition: FullComposition, hints: Array<String>?)

    @Skip
    fun showEnterAlbumArtistDialog(composition: FullComposition, hints: Array<String>?)

    @Skip
    fun showEnterLyricsDialog(composition: FullComposition)

    @Skip
    fun showAddGenreDialog(genres: Array<String>?)

    @Skip
    fun showEditGenreDialog(shortGenre: ShortGenre, genres: Array<String>?)

    @AddToEndSingle
    fun showGenres(shortGenres: List<ShortGenre>)

    @OneExecution
    fun showRemovedGenreMessage(genre: ShortGenre)

    @Skip
    fun showCoverActionsDialog()

    @Skip
    fun showSelectImageFromGalleryScreen()

}