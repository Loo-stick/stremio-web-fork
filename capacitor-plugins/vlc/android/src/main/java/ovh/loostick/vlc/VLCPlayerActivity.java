package ovh.loostick.vlc;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.List;

public class VLCPlayerActivity extends AppCompatActivity implements MediaPlayer.EventListener {

    private static final String TAG = "VLCPlayerActivity";
    private static VLCPlayerActivity instance;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;

    private String videoUrl;
    private String initialSubtitle;
    private int startTime;

    private Handler timeUpdateHandler;
    private Runnable timeUpdateRunnable;
    private boolean isPlaying = false;

    // UI Controls
    private View controlsOverlay;
    private ImageButton playPauseButton;
    private ImageButton backButton;
    private SeekBar seekBar;
    private TextView currentTimeText;
    private TextView durationText;
    private boolean controlsVisible = true;
    private Handler hideControlsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        // Set fullscreen
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_vlc_player);

        // Get intent extras
        videoUrl = getIntent().getStringExtra("url");
        initialSubtitle = getIntent().getStringExtra("subtitle");
        startTime = getIntent().getIntExtra("startTime", 0);

        Log.d(TAG, "onCreate() - URL: " + videoUrl + ", Subtitle: " + initialSubtitle);

        // Initialize views
        videoLayout = findViewById(R.id.video_layout);
        controlsOverlay = findViewById(R.id.controls_overlay);
        playPauseButton = findViewById(R.id.play_pause_button);
        backButton = findViewById(R.id.back_button);
        seekBar = findViewById(R.id.seek_bar);
        currentTimeText = findViewById(R.id.current_time);
        durationText = findViewById(R.id.duration);

        setupControls();
        initializePlayer();
    }

    private void setupControls() {
        hideControlsHandler = new Handler(Looper.getMainLooper());

        videoLayout.setOnClickListener(v -> toggleControls());

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pause();
            } else {
                resume();
            }
        });

        backButton.setOnClickListener(v -> finish());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.setTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopHideControlsTimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startHideControlsTimer();
            }
        });
    }

    private void toggleControls() {
        if (controlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        controlsOverlay.setVisibility(View.VISIBLE);
        controlsVisible = true;
        startHideControlsTimer();
    }

    private void hideControls() {
        controlsOverlay.setVisibility(View.GONE);
        controlsVisible = false;
        stopHideControlsTimer();
    }

    private void startHideControlsTimer() {
        stopHideControlsTimer();
        hideControlsHandler.postDelayed(this::hideControls, 3000);
    }

    private void stopHideControlsTimer() {
        hideControlsHandler.removeCallbacksAndMessages(null);
    }

    private void initializePlayer() {
        try {
            // Initialize LibVLC
            ArrayList<String> options = new ArrayList<>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch");
            options.add("-vvv"); // Verbose logging for debugging

            libVLC = new LibVLC(this, options);
            mediaPlayer = new MediaPlayer(libVLC);
            mediaPlayer.setEventListener(this);

            // Attach video output
            mediaPlayer.attachViews(videoLayout, null, true, false);

            // Create and configure media
            Media media = new Media(libVLC, Uri.parse(videoUrl));
            media.setHWDecoderEnabled(true, false);

            // Add initial subtitle if provided
            if (initialSubtitle != null && !initialSubtitle.isEmpty()) {
                Log.d(TAG, "Adding initial subtitle: " + initialSubtitle);
                media.addSlave(new IMedia.Slave(IMedia.Slave.Type.Subtitle, 4, initialSubtitle));
            }

            mediaPlayer.setMedia(media);
            media.release();

            // Start playback
            mediaPlayer.play();

            // Seek to start time if specified
            if (startTime > 0) {
                mediaPlayer.setTime(startTime);
            }

            // Setup time update handler
            timeUpdateHandler = new Handler(Looper.getMainLooper());
            timeUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && isPlaying) {
                        long time = mediaPlayer.getTime();
                        long duration = mediaPlayer.getLength();
                        updateTimeUI(time, duration);

                        // Notify JavaScript
                        VLCPlugin plugin = VLCPlugin.getInstance();
                        if (plugin != null) {
                            plugin.notifyTimeUpdate(time, duration);
                        }
                    }
                    timeUpdateHandler.postDelayed(this, 1000);
                }
            };
            timeUpdateHandler.post(timeUpdateRunnable);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing player", e);
            VLCPlugin plugin = VLCPlugin.getInstance();
            if (plugin != null) {
                plugin.notifyError("Failed to initialize player: " + e.getMessage());
            }
            finish();
        }
    }

    private void updateTimeUI(long time, long duration) {
        runOnUiThread(() -> {
            currentTimeText.setText(formatTime(time));
            durationText.setText(formatTime(duration));
            if (duration > 0) {
                seekBar.setMax((int) duration);
                seekBar.setProgress((int) time);
            }
        });
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        VLCPlugin plugin = VLCPlugin.getInstance();

        switch (event.type) {
            case MediaPlayer.Event.Playing:
                Log.d(TAG, "Event: Playing");
                isPlaying = true;
                runOnUiThread(() -> playPauseButton.setImageResource(android.R.drawable.ic_media_pause));
                if (plugin != null) {
                    plugin.notifyPlaying();
                }
                break;

            case MediaPlayer.Event.Paused:
                Log.d(TAG, "Event: Paused");
                isPlaying = false;
                runOnUiThread(() -> playPauseButton.setImageResource(android.R.drawable.ic_media_play));
                if (plugin != null) {
                    plugin.notifyPaused();
                }
                break;

            case MediaPlayer.Event.Stopped:
                Log.d(TAG, "Event: Stopped");
                isPlaying = false;
                break;

            case MediaPlayer.Event.EndReached:
                Log.d(TAG, "Event: EndReached");
                isPlaying = false;
                if (plugin != null) {
                    plugin.notifyEnded();
                }
                runOnUiThread(this::finish);
                break;

            case MediaPlayer.Event.EncounteredError:
                Log.e(TAG, "Event: EncounteredError");
                if (plugin != null) {
                    plugin.notifyError("Playback error occurred");
                }
                runOnUiThread(this::finish);
                break;

            case MediaPlayer.Event.Vout:
                Log.d(TAG, "Event: Vout - Video output available");
                // Update subtitle tracks when video is ready
                updateSubtitleTracksNotification();
                break;
        }
    }

    private void updateSubtitleTracksNotification() {
        VLCPlugin plugin = VLCPlugin.getInstance();
        if (plugin != null) {
            plugin.notifySubtitleTracksChanged(getSubtitleTracks());
        }
    }

    // Public methods called from VLCPlugin

    public static VLCPlayerActivity getInstance() {
        return instance;
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        finish();
    }

    public void seekTo(long time) {
        if (mediaPlayer != null) {
            mediaPlayer.setTime(time);
        }
    }

    public int addSubtitle(String url, String name, boolean select) {
        Log.d(TAG, "addSubtitle() - URL: " + url + ", select: " + select);

        if (mediaPlayer != null) {
            boolean result = mediaPlayer.addSlave(IMedia.Slave.Type.Subtitle, Uri.parse(url), select);
            Log.d(TAG, "addSlave result: " + result);

            // Get the new track count to return an ID
            int trackCount = mediaPlayer.getSpuTracksCount();

            // Update JavaScript about available tracks
            updateSubtitleTracksNotification();

            return trackCount - 1; // Return last track ID
        }
        return -1;
    }

    public JSArray getSubtitleTracks() {
        JSArray tracks = new JSArray();

        if (mediaPlayer != null) {
            MediaPlayer.TrackDescription[] spuTracks = mediaPlayer.getSpuTracks();
            if (spuTracks != null) {
                for (MediaPlayer.TrackDescription track : spuTracks) {
                    JSObject trackObj = new JSObject();
                    trackObj.put("id", track.id);
                    trackObj.put("name", track.name);
                    tracks.put(trackObj);
                }
            }
        }

        return tracks;
    }

    public void selectSubtitleTrack(int trackId) {
        if (mediaPlayer != null) {
            mediaPlayer.setSpuTrack(trackId);
        }
    }

    public void disableSubtitles() {
        if (mediaPlayer != null) {
            // Track ID -1 typically disables subtitles
            mediaPlayer.setSpuTrack(-1);
        }
    }

    public JSObject getState() {
        JSObject state = new JSObject();
        if (mediaPlayer != null) {
            state.put("isPlaying", isPlaying);
            state.put("time", mediaPlayer.getTime());
            state.put("duration", mediaPlayer.getLength());
        } else {
            state.put("isPlaying", false);
            state.put("time", 0);
            state.put("duration", 0);
        }
        return state;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't pause video when going to background - allow PiP
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;

        if (timeUpdateHandler != null) {
            timeUpdateHandler.removeCallbacksAndMessages(null);
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.detachViews();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (libVLC != null) {
            libVLC.release();
            libVLC = null;
        }

        Log.d(TAG, "onDestroy() - Player released");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopPlayer();
    }
}
