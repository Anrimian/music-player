package com.github.anrimian.musicplayer.ui.common.images.glide.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import javax.annotation.Nonnull;

public abstract class AppModelLoader<Model, Data> {

    public static <Model, Data> void addModelLoader(@Nonnull Registry registry,
                                                    @NonNull Class<Model> modelClass,
                                                    @NonNull Class<Data> dataClass,
                                                    @NonNull AppModelLoader<Model, Data> modelLoader) {
        registry.prepend(modelClass, dataClass, new SimpleLoaderFactory<>(dataClass, modelLoader));
    }

    protected void cleanup() {

    }

    protected void cancel() {

    }

    @NonNull
    protected DataSource getDataSource() {
        return DataSource.LOCAL;
    }

    protected abstract Object getModelKey(Model model);

    protected abstract void loadData(Model model,
                                     @NonNull Priority priority,
                                     @NonNull DataFetcher.DataCallback<? super Data> callback);

    private static class SimpleLoaderFactory<Model, Data> implements ModelLoaderFactory<Model, Data> {

        private final Class<Data> dataClass;
        private final AppModelLoader<Model, Data> appModelLoader;

        public SimpleLoaderFactory(Class<Data> dataClass, AppModelLoader<Model, Data> appModelLoader) {
            this.dataClass = dataClass;
            this.appModelLoader = appModelLoader;
        }

        @NonNull
        @Override
        public ModelLoader<Model, Data> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new SimpleModelLoader<>(dataClass, appModelLoader);
        }

        @Override
        public void teardown() {

        }

    }

    private static class SimpleModelLoader<Model, Data> implements ModelLoader<Model, Data> {

        private final Class<Data> dataClass;
        private final AppModelLoader<Model, Data> appModelLoader;

        private SimpleModelLoader(Class<Data> dataClass, AppModelLoader<Model, Data> appModelLoader) {
            this.dataClass = dataClass;
            this.appModelLoader = appModelLoader;
        }

        @Nullable
        @Override
        public LoadData<Data> buildLoadData(@NonNull Model model,
                                            int width,
                                            int height,
                                            @NonNull Options options) {
            return new LoadData<>(
                    new ObjectKey(appModelLoader.getModelKey(model)),
                    new SimpleDataFetcher<>(model, dataClass, appModelLoader)
            );
        }

        @Override
        public boolean handles(@NonNull Model model) {
            return true;
        }
    }

    private static class SimpleDataFetcher<Model, Data> implements DataFetcher<Data> {

        private final Model model;
        private final Class<Data> dataClass;
        private final AppModelLoader<Model, Data> appModelLoader;

        public SimpleDataFetcher(Model model,
                                 Class<Data> dataClass,
                                 AppModelLoader<Model, Data> appModelLoader) {
            this.model = model;
            this.dataClass = dataClass;
            this.appModelLoader = appModelLoader;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Data> callback) {
            appModelLoader.loadData(model, priority, callback);
        }

        @Override
        public void cleanup() {

        }

        @Override
        public void cancel() {

        }

        @NonNull
        @Override
        public Class<Data> getDataClass() {
            return dataClass;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }

    }

}