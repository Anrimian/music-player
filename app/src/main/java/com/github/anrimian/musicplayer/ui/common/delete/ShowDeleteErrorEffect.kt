package com.github.anrimian.musicplayer.ui.common.delete

import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.editor.common.EditorErrorCommand

data class ShowDeleteErrorEffect(val errorCommand: EditorErrorCommand) : BaseEffect
