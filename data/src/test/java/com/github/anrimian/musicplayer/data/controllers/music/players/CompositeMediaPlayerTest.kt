package com.github.anrimian.musicplayer.data.controllers.music.players

import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyFloat
import org.mockito.kotlin.*

class CompositeMediaPlayerTest {

    private val player1: AppMediaPlayer = mock()
    private val player2: AppMediaPlayer = mock()
    
    private lateinit var compositeMediaPlayer: CompositeMediaPlayer
    
    private val inOrder = inOrder(player1, player2)
    
    private val player1ErrorEventSubject = PublishSubject.create<MediaPlayerEvent>()
    private val player2ErrorEventSubject = PublishSubject.create<MediaPlayerEvent>()
    
    private val player1PositionSubject = PublishSubject.create<Long>()
    private val player2PositionSubject = PublishSubject.create<Long>()

    private lateinit var textEventObserver: TestObserver<MediaPlayerEvent>
    
    @BeforeEach
    fun setUp() {
        whenever(player1.prepareToPlay(any(), anyOrNull())).thenReturn(Completable.complete())
        whenever(player1.getTrackPositionObservable()).thenReturn(player1PositionSubject)
        whenever(player1.getPlayerEventsObservable()).thenReturn(player1ErrorEventSubject)
        whenever(player1.getSpeedChangeAvailableObservable()).thenReturn(Observable.just(true))

        whenever(player2.prepareToPlay(any(), anyOrNull())).thenReturn(Completable.complete())
        whenever(player2.getTrackPositionObservable()).thenReturn(player2PositionSubject)
        whenever(player2.getPlayerEventsObservable()).thenReturn(player2ErrorEventSubject)
        whenever(player2.getSpeedChangeAvailableObservable()).thenReturn(Observable.just(true))

        compositeMediaPlayer = CompositeMediaPlayer(
            arrayListOf({ player1 }, { player2 }), 
            1f,
            SoundBalance(1f, 1f)
        )
        textEventObserver = compositeMediaPlayer.getPlayerEventsObservable().test()
    }

    @Test
    fun testPlayersSwitchOnPrepare() {
        val source: CompositionContentSource = mock()
        
        whenever(player1.prepareToPlay(any(), anyOrNull()))
            .thenReturn(Completable.error(UnsupportedSourceException()))
        compositeMediaPlayer.prepareToPlay(source).subscribe()
        inOrder.verify(player1).prepareToPlay(eq(source), anyOrNull())
        inOrder.verify(player1).release()
        inOrder.verify(player2).prepareToPlay(eq(source), anyOrNull())
    }

    @Test
    fun testAllPlayersNotWorkingOnPrepare() {
        val source: CompositionContentSource = mock()

        whenever(player1.prepareToPlay(any(), anyOrNull()))
            .thenReturn(Completable.error(UnsupportedSourceException()))
        val secondException = UnsupportedSourceException()
        whenever(player2.prepareToPlay(any(), anyOrNull())).thenReturn(Completable.error(secondException))
        
        compositeMediaPlayer.prepareToPlay(source)
            .test()
            .assertError(secondException)
        inOrder.verify(player1).prepareToPlay(eq(source), anyOrNull())
        inOrder.verify(player1).release()
        inOrder.verify(player2).prepareToPlay(eq(source), anyOrNull())
        inOrder.verify(player2, never()).release()
    }

    @Test
    fun testPlayersSwitchOnEvent() {
        val exception = UnsupportedSourceException()
        player1ErrorEventSubject.onNext(MediaPlayerEvent.Error(exception))
        inOrder.verify(player1).release()
        inOrder.verify(player2).setPlaybackSpeed(anyFloat())
    }

    @Test
    fun testPlayersSwitchToTheEndOnEvent() {
        val exception = UnsupportedSourceException()

        player1ErrorEventSubject.onNext(MediaPlayerEvent.Error(exception))
        inOrder.verify(player1).release()
        inOrder.verify(player2).setPlaybackSpeed(anyFloat())
        player2ErrorEventSubject.onNext(MediaPlayerEvent.Error(exception))
        inOrder.verify(player1, never()).release()
        inOrder.verify(player2, never()).setPlaybackSpeed(anyFloat())
    }

    @Test
    fun `switch player on event and prepare same source`() {
        val source: CompositionContentSource = mock()
        val exception = UnsupportedSourceException()

        compositeMediaPlayer.prepareToPlay(source).subscribe()
        player1ErrorEventSubject.onNext(MediaPlayerEvent.Error(exception))
        compositeMediaPlayer.prepareToPlay(source).subscribe()

        inOrder.verify(player1).prepareToPlay(eq(source), anyOrNull())
        inOrder.verify(player1).release()
        inOrder.verify(player2).prepareToPlay(eq(source), anyOrNull())
        inOrder.verify(player2, never()).release()
        textEventObserver.assertValue { event -> event is MediaPlayerEvent.Error }
    }

    @Test
    fun `switch player on event and prepare different source`() {
        val source: CompositionContentSource = mock()
        val source2: CompositionContentSource = mock()
        val exception = UnsupportedSourceException()

        compositeMediaPlayer.prepareToPlay(source).subscribe()
        player1ErrorEventSubject.onNext(MediaPlayerEvent.Error(exception))
        compositeMediaPlayer.prepareToPlay(source2).subscribe()

        inOrder.verify(player1).prepareToPlay(eq(source), anyOrNull())
        inOrder.verify(player1).prepareToPlay(eq(source2), anyOrNull())
        inOrder.verify(player1, never()).release()
        inOrder.verify(player2, never()).prepareToPlay(eq(source), anyOrNull())
        inOrder.verify(player2, never()).release()
    }

}