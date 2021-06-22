package com.github.anrimian.musicplayer.infrastructure.service.music;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;

public class CompositionSourceModelHelper {

    public static boolean areSourcesTheSame(CompositionSource first, CompositionSource second) {
        if (first.getClass().equals(second.getClass())) {
            if (first instanceof LibraryCompositionSource) {
                return CompositionHelper.areSourcesTheSame(
                        ((LibraryCompositionSource) first).getComposition(),
                        ((LibraryCompositionSource) second).getComposition());
            }
            if (first instanceof UriCompositionSource) {
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
                        .loadImage(composition, bitmap -> {
                            //can we adapt glide to be able return bitmaps by uri
                            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                            mediaSession.setMetadata(metadataBuilder.build());
                        });
            }
            if (source instanceof UriCompositionSource) {
                Components.getAppComponent()
                        .imageLoader()
                        .loadImage((UriCompositionSource) source, bitmap -> {
                            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                            mediaSession.setMetadata(metadataBuilder.build());
                        });
            }
        } else {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, null);
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
        if (source instanceof UriCompositionSource) {
            UriCompositionSource uriSource = (UriCompositionSource) source;

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

    public static long getTrackPosition(@Nonnull CompositionSource source) {
        if (source instanceof LibraryCompositionSource) {
            return ((LibraryCompositionSource) source).getTrackPosition();
        }
        if (source instanceof UriCompositionSource) {
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
        if (source instanceof UriCompositionSource) {
            return coverImageLoader.loadNotificationImage((UriCompositionSource) source, onCompleted);
        }
        throw new IllegalStateException();
    }
}
