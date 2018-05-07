package com.github.anrimian.simplemusicplayer.data.utils.file;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class TestFileObserver extends FileObserver {

    private PublishSubject<FileObserverEvent> eventSubject = PublishSubject.create();

    static final String TAG = "FILEOBSERVER";
    /**
     * should be end with File.separator
     */
    String rootPath;
    static final int mask = (FileObserver.CREATE |
            FileObserver.DELETE |
            FileObserver.DELETE_SELF |
            FileObserver.MODIFY |
            FileObserver.MOVED_FROM |
            FileObserver.MOVED_TO |
            FileObserver.MOVE_SELF);

    public TestFileObserver(String root){
        super(root, mask);

        if (! root.endsWith(File.separator)){
            root += File.separator;
        }
        rootPath = root;
    }

    public void onEvent(int event, String path) {

        switch(event){
            case FileObserver.CREATE:
                eventSubject.onNext(new FileObserverEvent(EventType.CREATE, path));
                Log.d(TAG, "CREATE:" + rootPath + path);
                break;
            case FileObserver.DELETE:
                eventSubject.onNext(new FileObserverEvent(EventType.DELETE, path));
                Log.d(TAG, "DELETE:" + rootPath + path);
                break;
            case FileObserver.DELETE_SELF:
                Log.d(TAG, "DELETE_SELF:" + rootPath + path);
                break;
            case FileObserver.MODIFY:
                Log.d(TAG, "MODIFY:" + rootPath + path);
                break;
            case FileObserver.MOVED_FROM:
                Log.d(TAG, "MOVED_FROM:" + rootPath + path);
                break;
            case FileObserver.MOVED_TO:
                Log.d(TAG, "MOVED_TO:" + path);
                break;
            case FileObserver.MOVE_SELF:
                Log.d(TAG, "MOVE_SELF:" + path);
                break;
            default:
                // just ignore
                break;
        }
    }

    public void close(){
        super.finalize();
    }

    public Observable<FileObserverEvent> getEventObservable() {
        return eventSubject;
    }
}
