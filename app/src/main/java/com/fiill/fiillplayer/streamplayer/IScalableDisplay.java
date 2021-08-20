package com.fiill.fiillplayer.streamplayer;

/**
 * Interfaces to set the video view's size
 */

public interface IScalableDisplay {

    void setAspectRatio(int ratio);
    void setVideoSize(int videoWidth, int videoHeight);
}
