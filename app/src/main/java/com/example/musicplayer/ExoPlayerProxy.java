package com.example.musicplayer;

import android.content.Context;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

public class ExoPlayerProxy {
    private ExoPlayer exoPlayer;
    private Context context;
    public ExoPlayerProxy(Context context) {
        this.context = context;
        this.exoPlayer = new ExoPlayer.Builder(context).build(); // Actual player instance
    }

    public void play(MediaItem mediaItem) {
        if (exoPlayer != null) {
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }
    public void pause() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        }
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    // Additional delegated methods as necessary
}