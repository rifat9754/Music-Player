package com.example.musicplayer;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

public class MediaItemFactory {

    public static MediaItem createMediaItem(Song song) {
        return new MediaItem.Builder()
                .setUri(song.getUri())
                .setMediaMetadata(new MediaMetadata.Builder()
                        .setTitle(song.getTitle())
                        .setArtworkUri(song.getArtworkUri())
                        .build())
                .build();
    }
}
