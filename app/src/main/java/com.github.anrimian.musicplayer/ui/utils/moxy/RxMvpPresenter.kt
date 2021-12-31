package com.github.anrimian.musicplayer.ui.utils.moxy

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import moxy.MvpPresenter
import moxy.MvpView

abstract class RxMvpPresenter<T : MvpView>: MvpPresenter<T>() {

    protected val presenterDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        presenterDisposable.dispose()
    }

    protected fun Disposable.autoDispose() {
        presenterDisposable.add(this)
    }

    @Suppress("unused")
    protected fun Disposable.ignoreDisposable() {
    }
}