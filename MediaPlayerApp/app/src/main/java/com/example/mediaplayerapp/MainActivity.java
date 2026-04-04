package com.example.mediaplayerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // linking all UI elements
    private VideoView videoView;
    private TextView tvMediaLabel;
    private Button btnOpenFile, btnOpenUrl;
    private ImageButton btnPlay, btnPause, btnStop, btnRestart;

    // used for audio
    private MediaPlayer mediaPlayer;

    // check if current media is video or audio
    private boolean isVideoMode = false;

    // request code for permission
    private static final int PERMISSION_REQUEST_CODE = 100;

    // opens file picker and gets selected file
    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // User selected a file — load it as audio
                    loadAudio(uri);
                }
            });

    // main function where everything starts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect Java variables to XML views using their IDs
        videoView   = findViewById(R.id.videoView);
        tvMediaLabel = findViewById(R.id.tvMediaLabel);
        btnOpenFile  = findViewById(R.id.btnOpenFile);
        btnOpenUrl   = findViewById(R.id.btnOpenUrl);
        btnPlay      = findViewById(R.id.btnPlay);
        btnPause     = findViewById(R.id.btnPause);
        btnStop      = findViewById(R.id.btnStop);
        btnRestart   = findViewById(R.id.btnRestart);

        // button click actions
        btnOpenFile.setOnClickListener(v -> checkPermissionAndOpenFile());
        btnOpenUrl.setOnClickListener(v -> showUrlDialog());
        btnPlay.setOnClickListener(v -> playMedia());
        btnPause.setOnClickListener(v -> pauseMedia());
        btnStop.setOnClickListener(v -> stopMedia());
        btnRestart.setOnClickListener(v -> restartMedia());
    }

    // ── PERMISSION HANDLING ───────────────────────────────────────────────

    private void checkPermissionAndOpenFile() {
        // Android 13+ ( from tiramisu and after) uses READ_MEDIA_AUDIO instead of READ_EXTERNAL_STORAGE
        String permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            // ask for permission if not given
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted — open file picker
            openFilePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(this,
                        "Permission denied. Cannot open files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── FILE PICKER ───────────────────────────────────────────────────────

    private void openFilePicker() {
        // "audio/*" tells Android to show only audio files
        filePickerLauncher.launch("audio/*");
    }

    // ── AUDIO LOADING ─────────────────────────────────────────────────────

    private void loadAudio(Uri uri) {
        isVideoMode = false;

        // Hide the VideoView — not needed for audio
        videoView.setVisibility(android.view.View.GONE);

        // Release any previously loaded media to free memory
        releaseMediaPlayer();

        try {
            mediaPlayer = new MediaPlayer();
            // setDataSource tells MediaPlayer WHERE to get the audio from
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            // prepare() loads the file. prepareAsync() would be for network streams.
            mediaPlayer.prepare();
            tvMediaLabel.setText("Audio loaded: " + uri.getLastPathSegment());
            Toast.makeText(this, "Audio ready. Press Play.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading audio: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── URL DIALOG ────────────────────────────────────────────────────────

    private void showUrlDialog() {
        // Create an input field for the user to type a URL
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("https://example.com/video.mp4");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_URI);

        new AlertDialog.Builder(this)
                .setTitle("Enter Video URL")
                .setMessage("Paste a direct .mp4 video link:")
                .setView(input)
                .setPositiveButton("Load", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        loadVideo(url);
                    } else {
                        Toast.makeText(this, "URL cannot be empty",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── VIDEO LOADING ─────────────────────────────────────────────────────

    private void loadVideo(String url) {
        isVideoMode = true;

        // Release audio MediaPlayer if running
        releaseMediaPlayer();

        // Show the VideoView
        videoView.setVisibility(android.view.View.VISIBLE);

        // VideoView handles its own playback internally
        videoView.setVideoURI(Uri.parse(url));

        // MediaController adds a built-in seek bar and controls overlay
        android.widget.MediaController mc =
                new android.widget.MediaController(this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);

        // Prepare async — VideoView does this automatically when setVideoURI is called
        videoView.requestFocus();
        tvMediaLabel.setText("Video URL: " + url);
        Toast.makeText(this, "Video loading... Press Play.", Toast.LENGTH_SHORT).show();
    }

    // ── PLAYBACK CONTROLS ─────────────────────────────────────────────────

    private void playMedia() {
        if (isVideoMode) {
            videoView.start();
        } else if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            Toast.makeText(this, "Load a file or URL first.", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseMedia() {
        if (isVideoMode) {
            if (videoView.isPlaying()) videoView.pause();
        } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopMedia() {
        if (isVideoMode) {
            videoView.stopPlayback();
        } else if (mediaPlayer != null) {
            mediaPlayer.stop();
            // After stop(), you must call prepare() again before playing
            try { mediaPlayer.prepare(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void restartMedia() {
        if (isVideoMode) {
            videoView.seekTo(0);
            videoView.start();
        } else if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    // ── CLEANUP ───────────────────────────────────────────────────────────

    private void releaseMediaPlayer() {
        // Always release MediaPlayer when done to avoid memory leaks
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        // Called when the Activity is closed — clean up resources
        super.onDestroy();
        releaseMediaPlayer();
    }
}
