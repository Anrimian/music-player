package com.github.anrimian.musicplayer.infrastructure.service.music;

import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompositionSourceModelHelper {

    public static boolean areSourcesTheSame(@Nullable CompositionSource first, @Nullable CompositionSource second) {
        if (first == null || second == null) {
            return false;
        }

        if (first.getClass().equals(second.getClass())) {
            if (first instanceof LibraryCompositionSource) {
                return CompositionHelper.areSourcesTheSame(
                        ((LibraryCompositionSource) first).getComposition(),
                        ((LibraryCompositionSource) second).getComposition());
            }
            if (first instanceof ExternalCompositionSource) {
                return true;
            }
        }
        return false;
    }

    public static void updateMediaSessionAlbumArt(@Nullable CompositionSource source,
                                                  MediaMetadataCompat.Builder metadataBuilder,
                                                  MediaSessionCompat mediaSession,
                                                  boolean isEnabled) {
        if (isEnabled && source != null) {
            if (source instanceof LibraryCompositionSource) {
                Composition composition = ((LibraryCompositionSource) source).getComposition();
                Components.getAppComponent()
                        .imageLoader()
                        .loadImageUri(composition, uri -> {
                            String uriStr = null;
                            if (uri != null) {
                                uriStr = uri.toString();
                            }
                            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, uriStr);
                            mediaSession.setMetadata(metadataBuilder.build());
                        });

                //uri doesn't work for lock screen background, so put it here
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    Components.getAppComponent()
                            .imageLoader()
                            .loadImage(composition, bitmap -> {
                                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);
                                mediaSession.setMetadata(metadataBuilder.build());
                            });
                } else {
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null);
                    mediaSession.setMetadata(metadataBuilder.build());
                }
            }
            if (source instanceof ExternalCompositionSource) {
                Components.getAppComponent()
                        .imageLoader()
                        .loadImage((ExternalCompositionSource) source, bitmap -> {
                            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, null);
                            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);
                            mediaSession.setMetadata(metadataBuilder.build());
                        });
            }
        } else {
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, null);
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, null);
            mediaSession.setMetadata(metadataBuilder.build());
        }
    }

    public static void updateMediaSessionMetadata(@Nullable CompositionSource source,
                                                  MediaMetadataCompat.Builder metadataBuilder,
                                                  MediaSessionCompat mediaSession,
                                                  Context context) {
        if (source instanceof LibraryCompositionSource) {
            Composition composition = ((LibraryCompositionSource) source).getComposition();
            MediaMetadataCompat.Builder builder = metadataBuilder
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, formatCompositionName(composition))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, composition.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, formatCompositionAuthor(composition, context).toString())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, composition.getDuration());
            mediaSession.setMetadata(builder.build());
            return;
        }
        if (source instanceof ExternalCompositionSource) {
            ExternalCompositionSource uriSource = (ExternalCompositionSource) source;

            MediaMetadataCompat.Builder builder = metadataBuilder
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, formatCompositionName(uriSource.getTitle(), uriSource.getDisplayName()))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, uriSource.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, formatAuthor(uriSource.getArtist(), context).toString())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, uriSource.getDuration());
            mediaSession.setMetadata(builder.build());
            return;
        }
        MediaMetadataCompat.Builder builder = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, null)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, null)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, null)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0);
        mediaSession.setMetadata(builder.build());
    }

    public static long getTrackPosition(@Nullable CompositionSource source) {
        if (source instanceof LibraryCompositionSource) {
            return ((LibraryCompositionSource) source).getTrackPosition();
        }
        if (source instanceof ExternalCompositionSource) {
            return 0;
        }
        return 0;
    }

    public static Runnable getCompositionSourceCover(@Nonnull CompositionSource source,
                                                     Callback<Bitmap> onCompleted,
                                                     CoverImageLoader coverImageLoader) {
        if (source instanceof LibraryCompositionSource) {
            Composition composition = ((LibraryCompositionSource) source).getComposition();
            return coverImageLoader.loadNotificationImage(composition, onCompleted);
        }
        if (source instanceof ExternalCompositionSource) {
            return coverImageLoader.loadNotificationImage((ExternalCompositionSource) source, onCompleted);
        }
        throw new IllegalStateException();
    }
}
