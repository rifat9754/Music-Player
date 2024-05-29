
package com.example.musicplayer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.musicplayer.SongAdapter.SongViewHolder;
import com.google.android.exoplayer2.ExoPlayer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
public class SongAdapterTest {
    @Mock
    private Context mockContext;
    @Mock
    private ExoPlayer mockPlayer;
    @Mock
    private ConstraintLayout mockPlayerView;

    private SongAdapter adapter;
    private List<Song> songs;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        songs = Arrays.asList(new Song("Song 1", Uri.EMPTY, Uri.EMPTY, 1000, 180000),
                new Song("Song 2", Uri.EMPTY, Uri.EMPTY, 2000, 240000));
        adapter = new SongAdapter(mockContext, songs, mockPlayer, mockPlayerView);
    }

    @Test
    public void itemCount_IsCorrect() {
        assertEquals(2, adapter.getItemCount());
}



}
