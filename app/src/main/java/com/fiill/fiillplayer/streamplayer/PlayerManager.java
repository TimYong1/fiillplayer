package com.fiill.fiillplayer.streamplayer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * some common functions
 */

public class PlayerManager {

    public static final String TAG = "fiillPlayerManager";
    private volatile String currentPlayerFingerprint;
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
    private WeakReference<Activity> topActivityRef;

    public VideoInfo getDefaultVideoInfo() {
        return defaultVideoInfo;
    }

    /**
     * default config for all
     */
    private final VideoInfo defaultVideoInfo = new VideoInfo();


    public PlayerManager.MediaControllerGenerator getMediaControllerGenerator() {
        return MediaControllerGenerator;
    }

    //reserved. To set a new controller for with new views & style
    public void setMediaControllerGenerator(PlayerManager.MediaControllerGenerator mediaControllerGenerator) {
        MediaControllerGenerator = mediaControllerGenerator;
    }

    private MediaControllerGenerator MediaControllerGenerator = (context, videoInfo) -> new StreamMediaController(context);

    private final WeakHashMap<String, VideoView> videoViewsRef = new WeakHashMap<>();
    private final Map<String, StreamFiillPlayer> playersRef = new ConcurrentHashMap<>();
    private final WeakHashMap<Context, String> activity2playersRef = new WeakHashMap<>();


    private static final PlayerManager instance = new PlayerManager();


    public static PlayerManager getInstance() {
        return instance;
    }

    public StreamFiillPlayer getCurrentPlayer() {
        return currentPlayerFingerprint == null ? null : playersRef.get(currentPlayerFingerprint);
    }

    private StreamFiillPlayer createPlayer(VideoView videoView) {
        VideoInfo videoInfo = videoView.getVideoInfo();
        log(videoInfo.getFingerprint(), "createPlayer");
        videoViewsRef.put(videoInfo.getFingerprint(), videoView);
        registerActivityLifecycleCallbacks(((Activity) videoView.getContext()).getApplication());
        StreamFiillPlayer player = StreamFiillPlayer.createPlayer(videoView.getContext(), videoInfo);
        playersRef.put(videoInfo.getFingerprint(), player);
        activity2playersRef.put(videoView.getContext(), videoInfo.getFingerprint());
        if (topActivityRef == null || topActivityRef.get() == null) {
            topActivityRef = new WeakReference<>((Activity) videoView.getContext());
        }
        return player;
    }

    private synchronized void registerActivityLifecycleCallbacks(Application context) {
        if (activityLifecycleCallbacks != null) {
            return;
        }
        activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
//                System.out.println("======onActivityStarted============"+activity);

            }

            @Override
            public void onActivityResumed(Activity activity) {
                StreamFiillPlayer currentPlayer = getPlayerByFingerprint(activity2playersRef.get(activity));
                if (currentPlayer != null) {
                    currentPlayer.onActivityResumed();
                }
                topActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
//                System.out.println("======onActivityPaused============"+activity);
                StreamFiillPlayer currentPlayer = getPlayerByFingerprint(activity2playersRef.get(activity));
                if (currentPlayer != null) {
                    currentPlayer.onActivityPaused();
                }
                if (topActivityRef != null && topActivityRef.get() == activity) {
                    topActivityRef.clear();
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
//                System.out.println("======onActivityStopped============"+activity);

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                StreamFiillPlayer currentPlayer = getPlayerByFingerprint(activity2playersRef.get(activity));
                if (currentPlayer != null) {
                    currentPlayer.onActivityDestroyed();
                }
                activity2playersRef.remove(activity);
            }
        };
        context.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public void releaseCurrent() {
        log(currentPlayerFingerprint, "releaseCurrent");
        StreamFiillPlayer currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            if (currentPlayer.getProxyPlayerListener() != null) {
                currentPlayer.getProxyPlayerListener().onCompletion(currentPlayer);
            }
            currentPlayer.release();
        }
        currentPlayerFingerprint = null;
    }


    public boolean isCurrentPlayer(String fingerprint) {
        return fingerprint != null && fingerprint.equals(this.currentPlayerFingerprint);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        StreamFiillPlayer currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayer.onConfigurationChanged(newConfig);
        }
    }

    public boolean onBackPressed() {
        StreamFiillPlayer currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            return currentPlayer.onBackPressed();
        }
        return false;
    }

    public VideoView getVideoView(VideoInfo videoInfo) {
        return videoViewsRef.get(videoInfo.getFingerprint());
    }


    public void setCurrentPlayer(StreamFiillPlayer fiillPlayer) {
        VideoInfo videoInfo = fiillPlayer.getVideoInfo();
        log(videoInfo.getFingerprint(), "setCurrentPlayer");

        //if choose a new playerRef
        String fingerprint = videoInfo.getFingerprint();
        if (!isCurrentPlayer(fingerprint)) {
            try {
                log(videoInfo.getFingerprint(), "not same release before one:" + currentPlayerFingerprint);
                releaseCurrent();
                currentPlayerFingerprint = fingerprint;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            log(videoInfo.getFingerprint(), "is currentPlayer");
        }
    }

    public StreamFiillPlayer getPlayer(VideoView videoView) {
        VideoInfo videoInfo = videoView.getVideoInfo();
        StreamFiillPlayer player = playersRef.get(videoInfo.getFingerprint());
        if (player == null) {
            player = createPlayer(videoView);
        }
        return player;
    }

    public StreamFiillPlayer getPlayerByFingerprint(String fingerprint) {
        if (fingerprint == null) {
            return null;
        }
        return playersRef.get(fingerprint);
    }

    public PlayerManager releaseByFingerprint(String fingerprint) {
        StreamFiillPlayer player = playersRef.get(fingerprint);
        if (player != null) {
            player.release();
        }
        return this;
    }

    public void removePlayer(String fingerprint) {
        playersRef.remove(fingerprint);
    }

    private void log(String fingerprint, String msg) {
        if (StreamFiillPlayer.debug) {
            Log.d(TAG, String.format("[setFingerprint:%s] %s", fingerprint, msg));
        }
    }

    public Activity getTopActivity() {
        return topActivityRef.get();
    }


    /**
     * to create a custom MediaController
     */
    public interface MediaControllerGenerator {
        /**
         * called when VideoView need a MediaController
         * @param context activity context
         * @param videoInfo video informations, options
         * @return A playerListener/controller instance
         */
        IStreamMediaController create(Context context, VideoInfo videoInfo);
    }
}
