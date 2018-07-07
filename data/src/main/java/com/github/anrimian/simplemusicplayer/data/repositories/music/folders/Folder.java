package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.List;

import io.reactivex.Observable;

import static io.reactivex.subjects.PublishSubject.create;

public class Folder {

    private final List<FileSource> files;
    private final Observable<Change<List<FileSource>>> childChangeObservable;
    private final Observable<Change<FileSource>> selfChangeObservable;

    public Folder(List<FileSource> files,
                  Observable<Change<List<FileSource>>> childChangeObservable,
                  Observable<Change<FileSource>> selfChangeObservable) {
        this.files = files;
        this.childChangeObservable = childChangeObservable;
        this.selfChangeObservable = selfChangeObservable;
    }

    public List<FileSource> getFiles() {
        return files;
    }

    public Observable<Change<List<FileSource>>> getChildChangeObservable() {
        return childChangeObservable;
    }

    public Observable<Change<FileSource>> getSelfChangeObservable() {
        return selfChangeObservable;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "files=" + files +
                '}';
    }
}
