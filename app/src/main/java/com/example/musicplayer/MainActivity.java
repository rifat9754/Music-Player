package com.example.musicplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainActivity extends AppCompatActivity {
    // comment for main activity
    // arju
    private static final String TAG = "MainActivity";
    // arju 3

    // Members
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;

    ExoPlayer player;
    ActivityResultLauncher<String> recordAudioPermissionLauncher;
    final String recordAudioPermission=Manifest.permission.RECORD_AUDIO;
    ConstraintLayout playerView;
    TextView playerCloseBtn;

    //controls
    TextView songNameView,skipPreviousBtn,skipNextBtn,playPauseBtn,repeatModeBtn,playlistBtn;
    TextView homeSongNameView,homeSkipPreviousBtn,homePlayPauseBtn,homeSkipNextBtn;
    //wrappers
    ConstraintLayout homeControlWrapper,headWrapper,artworkWrapper,seekbarWrapper,controlWrapper,audioVisualizerWrapper;
    //artwork
    CircleImageView artworkView;
    //seekbar
    SeekBar seekbar;
    TextView progressView,durationView;
    //audio visualizer
    BarVisualizer audioVisualizer;
    //blur image view
    BlurImageView blurImageView;
    //status bar & nevigation color
    int defaultStatusColor;
    //repeat mode
    int repeatMode=1;//repeat all=1, repeat one=2, shuffle all=3


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //save the status color
        defaultStatusColor=getWindow().getStatusBarColor();
        //set the nevigation color
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));//0,255

        // Set the toolbar and app title
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerview);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                // Fetch songs
                fetchSongs();
            } else {
                userResponses();
            }
        });

        //launch storage permission on Create
        storagePermissionLauncher.launch(permission);

        //record audio permission
        recordAudioPermissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
            if(granted && player.isPlaying()){
                activateAudioVisualizer();
            }else{
                userResponsesOnRecordAudioperm();
            }

        });

//view
        player=new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.playerView);
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView); // Corrected from songNomeView
        skipPreviousBtn = findViewById(R.id.skipPreviousBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn); // Corrected from FindViewById
        playlistBtn = findViewById(R.id.playlistBtn);
        homeSongNameView = findViewById(R.id.homeSongNameView);
        homeSkipPreviousBtn = findViewById(R.id.homeSkipPreviousBtn);
        homeSkipNextBtn = findViewById(R.id.homeSkipNextBtn);
        homePlayPauseBtn = findViewById(R.id.homePlayPauseBtn); // Removed misplaced space

        // Wrappers
        homeControlWrapper = findViewById(R.id.homeControlWrapper);
        headWrapper = findViewById(R.id.headWrapper);
        artworkWrapper = findViewById(R.id.artworkWrapper);
        seekbarWrapper = findViewById(R.id.seekberWrapper);
        controlWrapper = findViewById(R.id.controlWrapper);
        audioVisualizerWrapper = findViewById(R.id.audioVisualizerWrapper);

// Artwork
        artworkView = findViewById(R.id.artworkView); // Corrected from R.1d.ortworkView

// Seek bar
        seekbar = findViewById(R.id.seekbar);
        progressView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.durationView);

// Audio visualizer
        audioVisualizer = findViewById(R.id.visualizer); // Corrected from findViewById(R.id.visualizer):
        // Blur image view
        blurImageView = findViewById(R.id.blurImageView);

        //player control method
        playerControls();




        // Launch storage permission on create
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Fetch songs if permission is already granted
            fetchSongs();
        } else {
            storagePermissionLauncher.launch(permission);
        }
    }
    // adding  COMMENT
    private void playerControls() {
        // Song name marquee
        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);

        // Exit the player view
        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
        playlistBtn.setOnClickListener(view -> exitPlayerView());

        //open player view on home control wrapper click
        homeControlWrapper.setOnClickListener(view ->showPlayerView());

        //player listener
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                //show the playing title
                assert mediaItem != null;
                songNameView.setText(mediaItem.mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);

                progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                seekbar.setProgress((int) player.getCurrentPosition());
                seekbar.setMax((int) player.getDuration());
                durationView.setText(getReadableTime((int) player.getDuration()));
                playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline1,0,0,0);
                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                //show current artwork
                showCurrentArtwork();
                //update the progress position of a current playing song
                updatePlayerPositionProgress();
                //load the artwork animation
                artworkView.setAnimation(loadRotation());
                //set audio visualizer
                activateAudioVisualizer();
                //update player view color
                updatePlayerColors();
                if(!player.isPlaying()){
                    player.play();
                }

            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState==ExoPlayer.STATE_READY){
                    //set values to player views
                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homeSongNameView.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    durationView.setText(getReadableTime((int) player.getDuration()));
                    seekbar.setMax((int) player.getDuration());
                    seekbar.setProgress((int) player.getCurrentPosition());
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline1,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);


                    //show current artwork
                    showCurrentArtwork();
                    //update the progress position of a current playing song
                    updatePlayerPositionProgress();
                    //load the artwork animation
                    artworkView.setAnimation(loadRotation());
                    //set audio visualizer
                    activateAudioVisualizer();
                    //update player view color
                    updatePlayerColors();
                }
                else{
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_outline1,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
                }
            }
        });

        //skip to next track

        skipNextBtn.setOnClickListener(view -> skipToNextSong());
        homeSkipNextBtn.setOnClickListener(view ->skipToNextSong());

        //skip to previous track
        skipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());
        homeSkipPreviousBtn.setOnClickListener(view ->skipToPreviousSong());

        //play or pause the player
        playPauseBtn.setOnClickListener(view ->playOrPausePlayer());
        homePlayPauseBtn.setOnClickListener(view -> playOrPausePlayer());

        //seek bar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue=0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue=seekBar.getProgress();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(player.getPlaybackState()==ExoPlayer.STATE_READY){
                    seekBar.setProgress(progressValue);
                    progressView.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }

            }
        });

        //repeat mode
        repeatModeBtn.setOnClickListener(view -> {
            if(repeatMode==1){
                //repeat one
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode=2;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_one,0,0,0);
            }else if(repeatMode==2){
                //shuffle all
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                repeatMode=3;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shuffle,0,0,0);
            } else if (repeatMode==3) {
                //repeat all
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                player.setShuffleModeEnabled(false);
                repeatMode=1;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_all,0,0,0);
            }
            //update colors
            updatePlayerColors();
        });
    }

    private void playOrPausePlayer() {
        if(player.isPlaying()){
            player.pause();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_outline1,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
            artworkView.clearAnimation();
        }else{
            player.play();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline1,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);
            artworkView.startAnimation(loadRotation());
        }
        //update player colors
        updatePlayerColors();
    }

    private void skipToPreviousSong() {
        if(player.hasPreviousMediaItem()){
            player.seekToPrevious();
        }
    }
    private void skipToNextSong() {
        if(player.hasNextMediaItem()){
            player.seekToNext();
        }
    }

    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(player.isPlaying()){
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekbar.setProgress((int) player.getCurrentPosition());
                }

                // repeat calling method
                updatePlayerPositionProgress();
                //load the artwork animation

                artworkView.setAnimation(loadRotation());

            }
        }, 1000);
    }

    private Animation loadRotation() {
        RotateAnimation rotateAnimation=new RotateAnimation(0,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    String getReadableTime(int duration) {
        String time;
        int hrs=duration/(1000*60*60);
        int min = (duration%(1000*60*60))/(1000*60);
        int secs=(((duration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;

        if(hrs<1){
            time=min + ":"+secs;
        }
        else{
            time=hrs+":"+min+":"+secs;
        }
        return  time;
    }


    private void showCurrentArtwork() {
        artworkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);
        if(artworkView.getDrawable()==null){
            artworkView.setImageResource(R.drawable.default_artwork);
        }
    }

    private void updatePlayerColors() {
        BitmapDrawable bitmapDrawable= (BitmapDrawable) artworkView.getDrawable();
        if(bitmapDrawable==null){
            bitmapDrawable= (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.default_artwork);
        }

        assert bitmapDrawable != null;
        Bitmap bmp=bitmapDrawable.getBitmap();

        //set bitmap to blur image view
        blurImageView.setImageBitmap(bmp);
        blurImageView.setBlur(4);

    }
    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColors();
    }


    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor, 199));
    }



    private void userResponsesOnRecordAudioperm() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(shouldShowRequestPermissionRationale(recordAudioPermission)){
                //show an educational UI explaining why we need this permission
                //use alert diagram
                new AlertDialog.Builder(this)
                        .setTitle("Requesting to show audio visualizer")
                        .setMessage("Allow this app to to display audio visualizer when music is playing")
                        .setPositiveButton("allow", (dialogInterface, i) -> {
                            //request the perm
                            recordAudioPermissionLauncher.launch(recordAudioPermission);
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> {
                            Toast.makeText(getApplicationContext(),"you denied to show the audio visualizer",Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        })
                        .show();
            }
            else{
                Toast.makeText(getApplicationContext(),"you denied to show the audio visualizer",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void activateAudioVisualizer() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //release the player
        if(player.isPlaying()){
            player.stop();
        }
        player.release();
    }

    private void userResponses() {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Fetch songs
            fetchSongs();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permission)) {
                // Show an educational UI to user explaining why we need this permission
                // Use alert dialog
                new AlertDialog.Builder(this)
                        .setTitle("Requesting Permission")
                        .setMessage("Allow us to fetch songs on your device")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Request permission
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "You denied us to show songs", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        } else {
            Toast.makeText(this, "You canceled to show songs", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongs() {
        // Define a list to carry songs
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        // Define projection
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        // Order
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        // Get the songs
        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, null, null, sortOrder)) {
            if (cursor != null) {
                // Cache cursor indices
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                // Clear the previous loaded before adding loading again
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    int duration = cursor.getInt(durationColumn);
                    int size = cursor.getInt(sizeColumn);
                    long albumId = cursor.getLong(albumColumn);

                    // Song uri
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                    // Album artwork uri
                    Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                    // Remove .mp3 extension from the song's name
                    if (name.lastIndexOf(".") > 0) {
                        name = name.substring(0, name.lastIndexOf("."));
                    }

                    // Song items
                    Song song = new Song(name, uri, albumArtworkUri, size, duration);

                    // Add song item to song list
                    songs.add(song);
                }

                // Display songs
                showSongs(songs);
            } else {
                Log.e(TAG, "Cursor is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch songs", e);
        }
    }

    private void showSongs(List<Song> songs) {
        if (songs.isEmpty()) {
            Toast.makeText(this, "No Songs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save songs
        allSongs.clear();
        allSongs.addAll(songs);

        // Update the toolbar title
        String title = getResources().getString(R.string.app_name) + " - " + songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        // Layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Songs adapter
        songAdapter = new SongAdapter(this, songs,player,playerView);

        // Set the adapter to RecyclerView
        recyclerView.setAdapter(songAdapter);

        //recyclerview animators optional
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(songAdapter);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());

        scaleInAnimationAdapter.setFirstOnly(false);
        recyclerView.setAdapter(scaleInAnimationAdapter);

    }

    //setting menu/search btn

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn,menu);

        //search btn item
        MenuItem menuItem=menu.findItem(R.id.searchBtn);
        SearchView searchView= (SearchView) menuItem.getActionView();

        //search song method
        SearchSong(searchView);


        return super.onCreateOptionsMenu(menu);
    }

    private void SearchSong(SearchView searchView) {

        //search view listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter songs
                filterSongs(newText.toLowerCase());
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();

        if (allSongs.size() > 0) {
            for (Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(song);
                }
            }
            if (songAdapter != null) {
                songAdapter.filterSongs(filteredList);
            }
        }
    }
}