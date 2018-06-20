package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.List;

import io.reactivex.Observable;

import static io.reactivex.subjects.PublishSubject.create;

public class Folder {

    private final List<NodeData> files;
    private final Observable<Change<List<NodeData>>> childChangeObservable;
    private final Observable<Change<NodeData>> selfChangeObservable;

    public Folder(List<NodeData> files,
                  Observable<Change<List<NodeData>>> childChangeObservable,
                  Observable<Change<NodeData>> selfChangeObservable) {
        this.files = files;
        this.childChangeObservable = childChangeObservable;
        this.selfChangeObservable = selfChangeObservable;
    }

    public List<NodeData> getFiles() {
        return files;
    }

    public Observable<Change<List<NodeData>>> getChildChangeObservable() {
        return childChangeObservable;
    }

    public Observable<Change<NodeData>> getSelfChangeObservable() {
        return selfChangeObservable;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "files=" + files +
                '}';
    }
}
