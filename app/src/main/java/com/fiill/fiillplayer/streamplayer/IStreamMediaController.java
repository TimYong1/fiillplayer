package com.fiill.fiillplayer.streamplayer;


/**
 * interface for BaseMediaController
 */

public interface IStreamMediaController extends IPlayerListener {
    /**
     * bind this media controller to video controllerView
     */
    void bind(VideoView videoView);
}
