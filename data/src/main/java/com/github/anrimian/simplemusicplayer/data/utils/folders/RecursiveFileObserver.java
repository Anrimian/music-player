package com.github.anrimian.simplemusicplayer.data.utils.folders;

import android.os.FileObserver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A FileObserver that observes all the files/folders within given directory
 * recursively. It automatically starts/stops monitoring new folders/files
 * created after starting the watch.
 */
public class RecursiveFileObserver extends FileObserver {

    private final Map<String, FileObserver> observers = new HashMap<>();

    private String path;

    private int mask;

    private EventListener eventListener;

    public RecursiveFileObserver(String path, EventListener listener) {
        this(path, ALL_EVENTS, listener);
    }

    public RecursiveFileObserver(String path, int mask, EventListener listener) {
        super(path, mask);
        this.path = path;
        this.mask = mask | FileObserver.CREATE | FileObserver.DELETE_SELF;
        eventListener = listener;
    }

    @Override
    public void startWatching() {
        Stack<String> stack = new Stack<>();
        stack.push(path);

        // Recursively watch all child directories
        while (!stack.empty()) {
            String parent = stack.pop();
            startWatching(parent);


            File path = new File(parent);
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (watch(file)) {
                        stack.push(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    @Override
    public void stopWatching() {
        synchronized (observers) {
            for (FileObserver observer : observers.values()) {
                observer.stopWatching();
            }
            observers.clear();
        }
    }

    @Override
    public void onEvent(int event, String path) {
        File file;
        if (path == null) {
            file = new File(this.path);
        } else {
            file = new File(this.path, path);
        }
        notify(event, file);
    }

    private boolean watch(File file) {
        return file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..");
    }

    private void stopWatching(String path) {
        synchronized (observers) {
            FileObserver observer = observers.remove(path);
            if (observer != null) {
                observer.stopWatching();
            }
        }
    }

    private void notify(int event, File file) {
        if (eventListener != null) {
            eventListener.onEvent(event & FileObserver.ALL_EVENTS, file);
        }
    }

    private void startWatching(String path) {
        synchronized (observers) {
            FileObserver observer = observers.remove(path);
            if (observer != null) {
                observer.stopWatching();
            }
            observer = new SingleFileObserver(path, mask);
            observer.startWatching();
            observers.put(path, observer);
        }
    }

    private class SingleFileObserver extends FileObserver {
        private String filePath;

        SingleFileObserver(String path, int mask) {
            super(path, mask);
            filePath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            File file;
            if (path == null) {
                file = new File(filePath);
            } else {
                file = new File(filePath, path);
            }

            switch (event & FileObserver.ALL_EVENTS) {
                case DELETE_SELF:
                    RecursiveFileObserver.this.stopWatching(filePath);
                    break;
                case CREATE:
                    if (watch(file)) {
                        RecursiveFileObserver.this.startWatching(file.getAbsolutePath());
                    }
                    break;
            }

            RecursiveFileObserver.this.notify(event, file);
        }
    }

    public interface EventListener {
        void onEvent(int event, File file);
    }
}