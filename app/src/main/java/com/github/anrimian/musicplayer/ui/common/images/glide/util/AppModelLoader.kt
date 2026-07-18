package com.github.anrimian.musicplayer.ui.common.images.glide.util

import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

abstract class AppModelLoader<Model : Any, Data> {

    companion object {
        fun <Model : Any, Data> addModelLoader(
            registry: Registry,
            modelClass: Class<Model>,
            dataClass: Class<Data>,
            modelLoader: AppModelLoader<Model, Data>
        ) {
            registry.prepend(
                modelClass,
                dataClass,
                SimpleLoaderFactory(dataClass, modelLoader)
            )
        }
    }

    protected open fun cleanup() {}

    protected open fun cancel() {}

    protected open fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    protected abstract fun getModelKey(model: Model): Any

    protected abstract fun loadData(
        model: Model,
        priority: Priority,
        callback: DataFetcher.DataCallback<in Data>
    )

    private class SimpleLoaderFactory<Model : Any, Data>(
        private val dataClass: Class<Data>,
        private val appModelLoader: AppModelLoader<Model, Data>
    ) : ModelLoaderFactory<Model, Data> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Model, Data> {
            return SimpleModelLoader(dataClass, appModelLoader)
        }

        override fun teardown() {}
    }

    private class SimpleModelLoader<Model : Any, Data>(
        private val dataClass: Class<Data>,
        private val appModelLoader: AppModelLoader<Model, Data>
    ) : ModelLoader<Model, Data> {

        override fun buildLoadData(
            model: Model,
            width: Int,
            height: Int,
            options: Options
        ): ModelLoader.LoadData<Data> {
            return ModelLoader.LoadData(
                ObjectKey(appModelLoader.getModelKey(model)),
                SimpleDataFetcher(model, dataClass, appModelLoader)
            )
        }

        override fun handles(model: Model): Boolean {
            return true
        }
    }

    private class SimpleDataFetcher<Model : Any, Data>(
        private val model: Model,
        private val dataClass: Class<Data>,
        private val appModelLoader: AppModelLoader<Model, Data>
    ) : DataFetcher<Data> {

        override fun loadData(
            priority: Priority,
            callback: DataFetcher.DataCallback<in Data>
        ) {
            appModelLoader.loadData(model, priority, callback)
        }

        override fun cleanup() {
            appModelLoader.cleanup()
        }

        override fun cancel() {
            appModelLoader.cancel()
        }

        override fun getDataClass(): Class<Data> {
            return dataClass
        }

        override fun getDataSource(): DataSource {
            return appModelLoader.getDataSource()
        }
    }
}