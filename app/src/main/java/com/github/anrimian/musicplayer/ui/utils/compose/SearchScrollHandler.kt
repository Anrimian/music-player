package com.github.anrimian.musicplayer.ui.utils.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// has unfinished scenarios(search in middle of the list+clear search), check them later
@Composable
fun <T> SearchScrollHandler(
    listState: LazyListState,
    searchQuery: String?,
    dataState: T,
    isContentReady: (T) -> Boolean,
    getItems: (T) -> List<*>?
) {
    var savedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var savedOffset by rememberSaveable { mutableIntStateOf(0) }

    var previousSearchQuery by rememberSaveable { mutableStateOf(searchQuery) }
    var staleFingerprint by rememberSaveable { mutableIntStateOf(0) }

    val list = getItems(dataState)

    val currentFingerprint by remember(list) {
        derivedStateOf {
            if (list.isNullOrEmpty()) {
                0
            } else {
                var result = list.size
                result = 31 * result + (list.first()?.hashCode() ?: 0)
                result = 31 * result + (list.last()?.hashCode() ?: 0)
                result
            }
        }
    }

    LaunchedEffect(searchQuery, currentFingerprint) {
        val oldQuery = previousSearchQuery
        val newQuery = searchQuery

        if (newQuery != oldQuery) {
            if (!newQuery.isNullOrEmpty()) {
                if (oldQuery.isNullOrEmpty()) {
                    if (savedIndex == -1) {
                        savedIndex = listState.firstVisibleItemIndex
                        savedOffset = listState.firstVisibleItemScrollOffset
                    }
                }
                listState.scrollToItem(0)
            } else {
                staleFingerprint = currentFingerprint
            }
        }
        previousSearchQuery = newQuery

        val isQueryEmpty = searchQuery.isNullOrEmpty()
        val hasSavedPos = savedIndex != -1
        val isReady = isContentReady(dataState)

        if (isQueryEmpty && hasSavedPos && isReady) {
            val isFreshData = currentFingerprint != staleFingerprint

            if (isFreshData) {
                val count = list?.size ?: 0
                if (savedIndex < count) {
                    listState.scrollToItem(savedIndex, savedOffset)
                }

                savedIndex = -1
                savedOffset = 0
                staleFingerprint = 0
            }
        }
    }
}