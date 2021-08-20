package com.fiill.fiillplayer.streamplayer;

import android.util.Log;

import tv.danmaku.ijk.media.player.IjkTimedText;


/**
 * This proxy to choose which PlayerListener to use,
 * either DefaultMediaController or DefaultPlayerListener
 */

public class ProxyPlayerListener implements IPlayerListener {
    private static final String TAG = "fiillListener";
    private final VideoInfo videoInfo;

    public ProxyPlayerListener(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public IPlayerListener getOuterListener() {
        return outerListener;
    }

    private IPlayerListener outerListener;




    public void setOuterListener(IPlayerListener outerListener) {
        this.outerListener = outerListener;
    }

    private IPlayerListener outerListener() {
        if (outerListener != null) {
            return outerListener;
        }
        VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
        if (videoView != null && videoView.getPlayerListener() != null) {
            return videoView.getPlayerListener();
        }
        return DefaultPlayerListener.INSTANCE;
    }

    private IPlayerListener listener() {
        VideoView videoView = PlayerManager.getInstance().getVideoView(videoInfo);
        if (videoView != null && videoView.getMediaController() != null) {
            return videoView.getMediaController();
        }
        /*  debug codes
		if (videoView != null && null != videoView.getPlayerListener()) {
            return videoView.getPlayerListener();
        }
		*/
        return DefaultPlayerListener.INSTANCE;
    }

    @Override
    public void onPrepared(StreamFiillPlayer fiillPlayer) {
        log("onPrepared");
        listener().onPrepared(fiillPlayer);
        outerListener().onPrepared(fiillPlayer);
    }

    @Override
    public void onBufferingUpdate(StreamFiillPlayer fiillPlayer, int percent) {
//        if (fiillPlayer.debug) {
//            log("onBufferingUpdate:"+percent);
//        }
        listener().onBufferingUpdate(fiillPlayer,percent);
        outerListener().onBufferingUpdate(fiillPlayer,percent);
    }

    @Override
    public boolean onInfo(StreamFiillPlayer fiillPlayer, int what, int extra) {
        if (StreamFiillPlayer.debug) {
            log("onInfo:"+what+","+extra);
        }
        listener().onInfo(fiillPlayer,what,extra);
        return outerListener().onInfo(fiillPlayer,what,extra);
    }

    @Override
    public void onCompletion(StreamFiillPlayer fiillPlayer) {
        log("onCompletion");
        listener().onCompletion(fiillPlayer);
        outerListener().onCompletion(fiillPlayer);
    }

    @Override
    public void onSeekComplete(StreamFiillPlayer fiillPlayer) {
        log("onSeekComplete");
        listener().onSeekComplete(fiillPlayer);
        outerListener().onSeekComplete(fiillPlayer);

    }

    @Override
    public boolean onError(StreamFiillPlayer fiillPlayer, int what, int extra) {
        if (StreamFiillPlayer.debug) {
            log("onError:"+what+","+extra);
        }
        listener().onError(fiillPlayer,what,extra);
        return outerListener().onError(fiillPlayer,what,extra);
    }

    @Override
    public void onPause(StreamFiillPlayer fiillPlayer) {
        log("onPause");
        listener().onPause(fiillPlayer);
        outerListener().onPause(fiillPlayer);
    }

    @Override
    public void onRelease(StreamFiillPlayer fiillPlayer) {
        log("onRelease");
        listener().onRelease(fiillPlayer);
        outerListener().onRelease(fiillPlayer);

    }

    @Override
    public void onStart(StreamFiillPlayer fiillPlayer) {
        log("onStart");
        listener().onStart(fiillPlayer);
        outerListener().onStart(fiillPlayer);
    }

    @Override
    public void onTargetStateChange(int oldState, int newState) {
        if (StreamFiillPlayer.debug) {
            log("onTargetStateChange:"+oldState+"->"+newState);
        }
        listener().onTargetStateChange(oldState,newState);
        outerListener().onTargetStateChange(oldState,newState);
    }

    @Override
    public void onCurrentStateChange(int oldState, int newState) {
        if (StreamFiillPlayer.debug) {
            log("onCurrentStateChange:"+oldState+"->"+newState);
        }
        listener().onCurrentStateChange(oldState,newState);
        outerListener().onCurrentStateChange(oldState,newState);
    }

    @Override
    public void onDisplayModelChange(int oldModel, int newModel) {
        if (StreamFiillPlayer.debug) {
            log("onDisplayModelChange:"+oldModel+"->"+newModel);
        }
        listener().onDisplayModelChange(oldModel,newModel);
        outerListener().onDisplayModelChange(oldModel,newModel);
    }

    public void onPreparing(StreamFiillPlayer fiillPlayer) {
        log("onPreparing");
        listener().onPreparing(fiillPlayer);
        outerListener().onPreparing(fiillPlayer);
    }

    @Override
    public void onTimedText(StreamFiillPlayer fiillPlayer, IjkTimedText text) {
        if (StreamFiillPlayer.debug) {
            log("onTimedText:"+(text!=null?text.getText():"null"));
        }
        listener().onTimedText(fiillPlayer,text);
        outerListener().onTimedText(fiillPlayer,text);
    }

    @Override
    public void onLazyLoadProgress(StreamFiillPlayer fp,int progress) {
        if (StreamFiillPlayer.debug) {
            log("onLazyLoadProgress:"+progress);
        }
        listener().onLazyLoadProgress(fp,progress);
        outerListener().onLazyLoadProgress(fp,progress);
    }

    @Override
    public void onLazyLoadError(StreamFiillPlayer fiillPlayer, String message) {
        if (StreamFiillPlayer.debug) {
            log("onLazyLoadError:"+message);
        }
        listener().onLazyLoadError(fiillPlayer,message);
        outerListener().onLazyLoadError(fiillPlayer,message);
    }

    private void log(String msg) {
        if (StreamFiillPlayer.debug) {
            Log.d(TAG, String.format("[fingerprint:%s] %s", videoInfo.getFingerprint(), msg));
        }
    }

    /**
     * default player controller if no MediaController.
     */

    public static class DefaultPlayerListener implements IPlayerListener {

        public static final DefaultPlayerListener INSTANCE = new DefaultPlayerListener();

        @Override
        public void onPrepared(StreamFiillPlayer fiillplayer) { }

        @Override
        public void onBufferingUpdate(StreamFiillPlayer fiillplayer, int percent) { }

        @Override
        public boolean onInfo(StreamFiillPlayer fp, int what, int extra) { return true;}

        @Override
        public void onCompletion(StreamFiillPlayer fiillplayer) { }

        @Override
        public void onSeekComplete(StreamFiillPlayer fiillplayer) { }

        @Override
        public boolean onError(StreamFiillPlayer fiillplayer, int what, int extra) {return true;}

        @Override
        public void onPause(StreamFiillPlayer fiillplayer) { }

        @Override
        public void onRelease(StreamFiillPlayer fiillplayer) { }

        @Override
        public void onStart(StreamFiillPlayer fiillplayer) { }

        @Override
        public void onTargetStateChange(int oldState, int newState) { }

        @Override
        public void onCurrentStateChange(int oldState, int newState) { }

        @Override
        public void onDisplayModelChange(int oldModel, int newModel) { }

        @Override
        public void onPreparing(StreamFiillPlayer fiillplayer) { }

        @Override
        public void onTimedText(StreamFiillPlayer fiillplayer, IjkTimedText text) { }

        @Override
        public void onLazyLoadError(StreamFiillPlayer fiillplayer, String message) { }

        @Override
        public void onLazyLoadProgress(StreamFiillPlayer fiillplayer,int progress) { }
    }
}
