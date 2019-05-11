package com.github.anrimian.musicplayer.domain.models.player.service;

public class MusicNotificationSetting {
    private final boolean showCovers;
    private final boolean coloredNotification;
    private final boolean coversOnLockScreen;

    public MusicNotificationSetting(boolean showCovers,
                                    boolean coloredNotification,
                                    boolean coversOnLockScreen) {
        this.showCovers = showCovers;
        this.coloredNotification = coloredNotification;
        this.coversOnLockScreen = coversOnLockScreen;
    }

    public boolean isShowCovers() {
        return showCovers;
    }

    public boolean isColoredNotification() {
        return coloredNotification;
    }

    public boolean isCoversOnLockScreen() {
        return coversOnLockScreen;
    }
}
