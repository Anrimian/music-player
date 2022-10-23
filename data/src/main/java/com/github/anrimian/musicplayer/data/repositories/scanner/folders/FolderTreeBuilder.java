package com.github.anrimian.musicplayer.data.repositories.scanner.folders;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.domain.utils.functions.Mapper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class FolderTreeBuilder<F, N> {

    private final Mapper<F, String> pathFunc;
    private final Mapper<F, N> valueFunc;

    public FolderTreeBuilder(Mapper<F, String> pathFunc, Mapper<F, N> valueFunc) {
        this.pathFunc = pathFunc;
        this.valueFunc = valueFunc;
    }

    public FolderNode<N> createFileTree(LongSparseArray<F> map) {
        return createFileTree(fromSparseArray(map));
    }

    public FolderNode<N> createFileTree(Observable<F> objectsObservable) {
        FolderNode<N> rootFolder = new FolderNode<>(null);
        objectsObservable.groupBy(pathFunc::map)
                .doOnNext(group -> group.collect(ArrayList<F>::new, List::add)
                        .map(this::toValueList)
                        .doOnSuccess(list -> addValuesToFolder(rootFolder, group.getKey(), list))
                        .subscribe())
                .subscribe();
        return rootFolder;
    }

    private <T> Observable<T> fromSparseArray(LongSparseArray<T> sparseArray) {
        return Observable.create(emitter -> {
            for(int i = 0, size = sparseArray.size(); i < size; i++) {
                T existValue = sparseArray.valueAt(i);
                emitter.onNext(existValue);
            }
            emitter.onComplete();
        });
    }

    private List<N> toValueList(List<F> list) {
        return ListUtils.mapList(list, valueFunc::map);
    }

    private void addValuesToFolder(FolderNode<N> root,
                                   String path,
                                   List<N> values) {
        FolderNode<N> parent = getNode(root, path);
        parent.addFiles(values);
    }

    private FolderNode<N> getNode(FolderNode<N> root, String path) {
        if (TextUtils.isEmpty(path)) {
            return root;
        }

        FolderNode<N> target = root;

        String[] partialPaths = path.split("/");
        for (String partialPath : partialPaths) {
            FolderNode<N> child = target.getFolder(partialPath);
            if (child == null) {
                child = new FolderNode<>(partialPath);
                target.addFolder(child);
            }
            target = child;
        }
        return target;
    }

}
