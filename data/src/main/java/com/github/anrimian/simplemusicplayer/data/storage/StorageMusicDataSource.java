package com.github.anrimian.simplemusicplayer.data.storage;

import com.github.anrimian.simplemusicplayer.data.utils.Objects;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;


public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final Scheduler scheduler;

    @Nullable
    private ChangeableMap<Long, Composition> compositions;

    public StorageMusicDataSource(StorageMusicProvider musicProvider, Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.scheduler = scheduler;
    }

    public Single<ChangeableMap<Long, Composition>> getCompositions() {
        return Single.fromCallable(this::getList)
                .observeOn(scheduler);
    }

    private ChangeableMap<Long, Composition> getList() {
        if (compositions == null) {
            synchronized (StorageMusicDataSource.this) {
                if (compositions == null) {
                    compositions = createMap();
                }
            }
        }
        return compositions;
    }

    private ChangeableMap<Long, Composition> createMap() {
        Map<Long, Composition> hashMap = musicProvider.getCompositions();

        Observable<Change<Composition>> changeObservable = musicProvider.getChangeObservable()
                .flatMap(this::calculateChange)
                .share()
                .observeOn(scheduler);
        return new ChangeableMap<>(hashMap, changeObservable);
    }

    private Observable<Change<Composition>> calculateChange(Map<Long, Composition> newCompositions) {
        return Observable.create(emitter -> {
            List<Composition> deletedCompositions = new ArrayList<>();
            List<Composition> addedCompositions = new ArrayList<>();
            List<Composition> changedCompositions = new ArrayList<>();

            Map<Long, Composition> existsCompositions = compositions.getHashMap();

            for (Composition existComposition: new HashMap<>(existsCompositions).values()) {
                Composition newComposition = newCompositions.get(existComposition.getId());
                if (newComposition == null) {
                    deletedCompositions.add(existComposition);
                    existsCompositions.remove(existComposition.getId());
                }
            }

            for (Composition newComposition: newCompositions.values()) {
                Composition existComposition = existsCompositions.get(newComposition.getId());
                if (existComposition == null) {
                    addedCompositions.add(newComposition);
                    existsCompositions.put(newComposition.getId(), newComposition);
                } else if (hasChanges(newComposition, existComposition)) {
                    changedCompositions.add(newComposition);
                    existsCompositions.put(newComposition.getId(), newComposition);
                }
            }

            if (!deletedCompositions.isEmpty()) {
                emitter.onNext(new Change<>(ChangeType.DELETED, deletedCompositions));
            }
            if (!addedCompositions.isEmpty()) {
                emitter.onNext(new Change<>(ChangeType.ADDED, addedCompositions));
            }
            if (!changedCompositions.isEmpty()) {
                emitter.onNext(new Change<>(ChangeType.MODIFY, changedCompositions));
            }
        });
    }

    private boolean hasChanges(@Nonnull Composition first, @Nonnull Composition second) {
        return !Objects.equals(first.getAlbum(), second.getAlbum())
                || !Objects.equals(first.getArtist(), second.getArtist())
                || !Objects.equals(first.getComposer(), second.getComposer())
                || !Objects.equals(first.getDateAdded(), second.getDateAdded())
                || !Objects.equals(first.getDateModified(), second.getDateModified())
                || !Objects.equals(first.getDisplayName(), second.getDisplayName())
                || first.getDuration() != second.getDuration()
                || !Objects.equals(first.getFilePath(), second.getFilePath())
                || first.getSize() != second.getSize()
                || !Objects.equals(first.getTitle(), second.getTitle())
                || !Objects.equals(first.getYear(), second.getYear());
    }
}
