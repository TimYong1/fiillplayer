package com.fiill.fiillplayer.streamplayer;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.fiill.fiillplayer.R;

/**
 * The is the base video view for live stream
 */

public class VideoView extends FrameLayout {


    private IStreamMediaController mediaController;
    private IPlayerListener playerListener;
    private ViewGroup container;

    public IPlayerListener getPlayerListener() {
        return playerListener;
    }

    public VideoView setPlayerListener(IPlayerListener playerListener) {
        this.playerListener = playerListener;
        return this;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void videoInfo(VideoInfo videoInfo) {
        if (this.videoInfo.getUri() != null && !this.videoInfo.getUri().equals(videoInfo.getUri())) {
            PlayerManager.getInstance().releaseByFingerprint(this.videoInfo.getFingerprint());
        }
        this.videoInfo = videoInfo;
    }

    private VideoInfo videoInfo = VideoInfo.createFromDefault();

    public VideoView(Context context) {
        super(context);
        init(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        container = new FrameLayout(context);
        addView(container, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initMediaController();
        setBackgroundColor(videoInfo.getBgColor());
    }

    private void initMediaController() {
        mediaController = PlayerManager.getInstance().getMediaControllerGenerator().create(getContext(), videoInfo);
        if (mediaController != null) {
            mediaController.bind(this);
        }
    }

    public VideoView setFingerprint(Object fingerprint) {
        videoInfo.setFingerprint(fingerprint);
        return this;
    }

    public void setVideoPath(String uri) {
        videoInfo.setUri(Uri.parse(uri));
    }

    public StreamFiillPlayer getPlayer() {
        if (videoInfo.getUri() == null) {
            throw new RuntimeException("player uri is null");
        }
        return PlayerManager.getInstance().getPlayer(this);
    }

    /**
     * is current active player (in list controllerView there are many players)
     *
     * @return boolean
     */
    public boolean isCurrentActivePlayer() {
        return PlayerManager.getInstance().isCurrentPlayer(videoInfo.getFingerprint());
    }

    public IStreamMediaController getMediaController() {
        return mediaController;
    }

    /**
     * is video controllerView in 'list' controllerView
     */
    public boolean inListView() {
        for (ViewParent vp = getParent(); vp != null; vp = vp.getParent()) {
            if (vp instanceof AbsListView
                    || vp instanceof ScrollView) {
                return true;
            }
        }
        return false;
    }

    public ViewGroup getContainer() {
        return container;
    }

    public ImageView getCoverView() {
        return (ImageView) findViewById(R.id.app_video_cover);
    }
}
