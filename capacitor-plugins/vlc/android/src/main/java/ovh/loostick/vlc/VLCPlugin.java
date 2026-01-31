package ovh.loostick.vlc;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "VLCPlugin")
public class VLCPlugin extends Plugin {

    private static final String TAG = "VLCPlugin";
    private static VLCPlugin instance;
    private PluginCall currentCall;

    @Override
    public void load() {
        super.load();
        instance = this;
        Log.d(TAG, "VLCPlugin loaded");
    }

    public static VLCPlugin getInstance() {
        return instance;
    }

    @PluginMethod
    public void play(PluginCall call) {
        String url = call.getString("url");
        if (url == null || url.isEmpty()) {
            call.reject("URL is required");
            return;
        }

        String title = call.getString("title", "");
        String subtitle = call.getString("subtitle");
        Integer startTime = call.getInt("startTime", 0);

        Log.d(TAG, "play() called with URL: " + url);

        currentCall = call;

        Intent intent = new Intent(getContext(), VLCPlayerActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        if (subtitle != null && !subtitle.isEmpty()) {
            intent.putExtra("subtitle", subtitle);
        }
        intent.putExtra("startTime", startTime);

        startActivityForResult(call, intent, "handlePlayerResult");
    }

    @ActivityCallback
    private void handlePlayerResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        JSObject ret = new JSObject();
        ret.put("result", result.getResultCode() == Activity.RESULT_OK);
        call.resolve(ret);

        // Notify that player was closed
        notifyListeners("playerClosed", new JSObject());
    }

    @PluginMethod
    public void pause(PluginCall call) {
        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            activity.pause();
            call.resolve();
        } else {
            call.reject("Player not active");
        }
    }

    @PluginMethod
    public void resume(PluginCall call) {
        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            activity.resume();
            call.resolve();
        } else {
            call.reject("Player not active");
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            activity.stopPlayer();
            call.resolve();
        } else {
            call.resolve(); // Already stopped
        }
    }

    @PluginMethod
    public void seekTo(PluginCall call) {
        Integer time = call.getInt("time");
        if (time == null) {
            call.reject("Time is required");
            return;
        }

        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            activity.seekTo(time);
            call.resolve();
        } else {
            call.reject("Player not active");
        }
    }

    @PluginMethod
    public void addSubtitle(PluginCall call) {
        String url = call.getString("url");
        if (url == null || url.isEmpty()) {
            call.reject("Subtitle URL is required");
            return;
        }

        String name = call.getString("name", "External Subtitle");
        Boolean select = call.getBoolean("select", true);

        Log.d(TAG, "addSubtitle() called with URL: " + url);

        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            int trackId = activity.addSubtitle(url, name, select);
            JSObject ret = new JSObject();
            ret.put("trackId", trackId);
            call.resolve(ret);
        } else {
            call.reject("Player not active");
        }
    }

    @PluginMethod
    public void getSubtitleTracks(PluginCall call) {
        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            JSArray tracks = activity.getSubtitleTracks();
            JSObject ret = new JSObject();
            ret.put("tracks", tracks);
            call.resolve(ret);
        } else {
            JSObject ret = new JSObject();
            ret.put("tracks", new JSArray());
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void selectSubtitleTrack(PluginCall call) {
        Integer trackId = call.getInt("trackId");
        if (trackId == null) {
            call.reject("Track ID is required");
            return;
        }

        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            activity.selectSubtitleTrack(trackId);
            call.resolve();
        } else {
            call.reject("Player not active");
        }
    }

    @PluginMethod
    public void disableSubtitles(PluginCall call) {
        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            activity.disableSubtitles();
            call.resolve();
        } else {
            call.reject("Player not active");
        }
    }

    @PluginMethod
    public void getState(PluginCall call) {
        VLCPlayerActivity activity = VLCPlayerActivity.getInstance();
        if (activity != null) {
            JSObject state = activity.getState();
            call.resolve(state);
        } else {
            JSObject state = new JSObject();
            state.put("isPlaying", false);
            state.put("time", 0);
            state.put("duration", 0);
            call.resolve(state);
        }
    }

    // Methods to notify JavaScript listeners from the Activity
    public void notifyPlaying() {
        notifyListeners("playing", new JSObject());
    }

    public void notifyPaused() {
        notifyListeners("paused", new JSObject());
    }

    public void notifyEnded() {
        notifyListeners("ended", new JSObject());
    }

    public void notifyTimeUpdate(long time, long duration) {
        JSObject data = new JSObject();
        data.put("time", time);
        data.put("duration", duration);
        notifyListeners("timeUpdate", data);
    }

    public void notifyError(String message) {
        JSObject data = new JSObject();
        data.put("message", message);
        notifyListeners("error", data);
    }

    public void notifySubtitleTracksChanged(JSArray tracks) {
        JSObject data = new JSObject();
        data.put("tracks", tracks);
        notifyListeners("subtitleTracksChanged", data);
    }
}
