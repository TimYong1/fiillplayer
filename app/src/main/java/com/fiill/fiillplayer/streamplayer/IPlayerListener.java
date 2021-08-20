package com.fiill.fiillplayer.streamplayer;

import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 *  Interface to adapter ijkplayer or android media player.
 *  either defaultPlayerListener or streamMediaController will implement it.
 */

public interface IPlayerListener {

    void onPrepared(StreamFiillPlayer fiillPlayer);

    /**
     * Called to update status in buffering a media stream received through progressive HTTP download.
     * @param fiillplayer the listening player
     * @param percent nt: the percentage (0-100) of the content that has been buffered or played thus far
     */
    void onBufferingUpdate(StreamFiillPlayer fiillplayer, int percent);

    boolean onInfo(StreamFiillPlayer fiillplayer, int what, int extra);

    void onCompletion(StreamFiillPlayer fiillplayer);

    void onSeekComplete(StreamFiillPlayer fiillplayer);

    boolean onError(StreamFiillPlayer fiillplayer,int what, int extra);

    void onPause(StreamFiillPlayer fiillplayer);

    void onRelease(StreamFiillPlayer fiillplayer);

    void onStart(StreamFiillPlayer fiillplayer);

    void onTargetStateChange(int oldState, int newState);

    void onCurrentStateChange(int oldState, int newState);

    void onDisplayModelChange(int oldModel, int newModel);

    void onPreparing(StreamFiillPlayer fiillPlayer);

    /**
     * render subtitle
     * @param fiillplayer the listening player
     * @param text timed text string
     */
    void onTimedText(StreamFiillPlayer fiillplayer,IjkTimedText text);

    void onLazyLoadProgress(StreamFiillPlayer fiillplayer,int progress);

    void onLazyLoadError(StreamFiillPlayer fiillplayer, String message);
}
