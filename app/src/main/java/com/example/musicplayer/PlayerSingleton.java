package com.example.musicplayer;

import android.content.Context;

import com.google.android.exoplayer2.ExoPlayer;

public class PlayerSingleton {
    private static ExoPlayer instance;

    private PlayerSingleton() { }

    public static ExoPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new ExoPlayer.Builder(context).build();
        }
        return instance;
}
}