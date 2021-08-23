package com.fiill.fiillplayer.streamplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.fiill.fiillplayer.R;

import java.util.Locale;

import com.fiill.fiillplayer.trackselector.TrackSelectorFragment;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * media controller for video view when float mode
 */

public class StreamMediaController implements IStreamMediaController,Handler.Callback {

    protected static final int STATUS_ERROR = -1;
    protected static final int STATUS_IDLE = 0;
    protected static final int STATUS_LOADING = 1;
    protected static final int STATUS_PLAYING = 2;
    protected static final int STATUS_PAUSE = 3;
    protected static final int STATUS_COMPLETED = 4;

    protected long newPosition = -1;
    protected boolean isShowing;
    protected boolean isDragging;

    protected boolean instantSeeking;
    protected SeekBar seekBar;

    protected int volume = -1;
    protected final int maxVolume;


    protected float brightness;
    private int status = STATUS_IDLE;
    private int displayModel = StreamFiillPlayer.DISPLAY_NORMAL;

    protected static final int MESSAGE_SHOW_PROGRESS = 1;
    protected static final int MESSAGE_FADE_OUT = 2;
    protected static final int MESSAGE_SEEK_NEW_POSITION = 3;
    protected static final int MESSAGE_HIDE_CENTER_BOX = 4;
    protected static final int MESSAGE_RESTART_PLAY = 5;

    protected final Context context;
    protected final AudioManager audioManager;
    protected ViewQuery viewquery;

    protected int defaultTimeout = 3 * 1000;
    protected Handler handler;
    protected VideoView videoView;
    protected View controllerView;


    @Override
    public void bind(VideoView videoView) {
        this.videoView = videoView;
        controllerView = makeControllerView();
        viewquery = new ViewQuery(controllerView);
        initView(controllerView);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.videoView.getContainer().addView(controllerView, layoutParams);
    }

    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    protected final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;
            if (!videoView.isCurrentActivePlayer()) {
                return;
            }
            viewquery.id(R.id.app_video_status).gone();//hide image when moving
            StreamFiillPlayer player = videoView.getPlayer();
            int newPosition = (int) (player.getDuration() * (progress * 1.0 / 1000));
            String time = generateTime(newPosition);
            if (instantSeeking) {
                player.seekTo(newPosition);

            }
            viewquery.id(R.id.app_video_currentTime).text(time);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            show(3600000);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            if (instantSeeking) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!videoView.isCurrentActivePlayer()) {
                return;
            }
            StreamFiillPlayer player = videoView.getPlayer();
            if (!instantSeeking) {
                player.seekTo((int) (player.getDuration() * (seekBar.getProgress() * 1.0 / 1000)));
            }
            show(defaultTimeout);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isDragging = false;
            handler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        }
    };

    protected void updatePausePlay() {
        if (videoView.isCurrentActivePlayer()) {
            boolean playing = videoView.getPlayer().isPlaying();
            if (playing) {
                viewquery.id(R.id.app_video_play).image(R.drawable.ic_stop_white_24dp);
            } else {
                viewquery.id(R.id.app_video_play).image(R.drawable.ic_play_arrow_white_24dp);
            }
        } else {
            viewquery.id(R.id.app_video_play).image(R.drawable.ic_play_arrow_white_24dp);
            viewquery.id(R.id.app_video_currentTime).text("");
            viewquery.id(R.id.app_video_endTime).text("");
        }
    }


    protected long setProgress() {
        if (isDragging) {
            return 0;
        }
        //check player is active
        boolean currentPlayer = videoView.isCurrentActivePlayer();
        if (!currentPlayer) {
            seekBar.setProgress(0);
            return 0;
        }

        //check player is ready
        StreamFiillPlayer player = videoView.getPlayer();
        int currentState = player.getCurrentState();
        if (currentState == StreamFiillPlayer.STATE_IDLE ||
                currentState == StreamFiillPlayer.STATE_PREPARING ||
                currentState == StreamFiillPlayer.STATE_ERROR) {
            return 0;
        }

        long position = player.getCurrentPosition();
        int duration = player.getDuration();

        if (seekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
            int percent = player.getBufferPercentage();
            seekBar.setSecondaryProgress(percent * 10);
        }

        viewquery.id(R.id.app_video_currentTime).text(generateTime(position));
        if (duration == 0) {//live stream
            viewquery.id(R.id.app_video_endTime).text(R.string.fiill_player_live);
        } else {
            viewquery.id(R.id.app_video_endTime).text(generateTime(duration));
        }
        return position;
    }

    protected void show(int timeout) {
        if (!isShowing) {
            if (videoView.getVideoInfo().isShowTopBar() || displayModel == StreamFiillPlayer.DISPLAY_FULL_WINDOW) {
                viewquery.id(R.id.app_video_top_box).visible();
                viewquery.id(R.id.app_video_title).text(videoView.getVideoInfo().getTitle());
            } else {
                viewquery.id(R.id.app_video_top_box).gone();
            }
            showBottomControl(true);
            isShowing = true;
        }
        updatePausePlay();
        handler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
        handler.removeMessages(MESSAGE_FADE_OUT);
        if (timeout != 0) {
            handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_FADE_OUT), timeout);
        }
    }


    protected void showBottomControl(boolean show) {
        if (displayModel == StreamFiillPlayer.DISPLAY_FLOAT) {
            show = false;
        }
        viewquery.id(R.id.app_video_bottom_box).visibility(show ? View.VISIBLE : View.GONE);
    }

    protected void hide(boolean force) {
        if (force || isShowing) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            showBottomControl(false);
            viewquery.id(R.id.app_video_top_box).gone();
            isShowing = false;
        }
    }

    public StreamMediaController(Context ctx) {
        context = ctx;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        handler = new Handler(Looper.getMainLooper(),this);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    protected View makeControllerView() {
        return LayoutInflater.from(context).inflate(R.layout.fiill_media_controller, videoView, false);
    }

    protected final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            StreamFiillPlayer player;
            try {
                player = videoView.getPlayer();
            }catch (RuntimeException e) {
                return;
            }
            if (v.getId() == R.id.app_video_fullscreen) {
                player.toggleFullScreen();
            } else if (v.getId() == R.id.app_video_play) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            } else if (v.getId() == R.id.app_video_replay_icon) {
                player.seekTo(0);
                player.start();
//                videoView.seekTo(0);
//                videoView.start();
//                doPauseResume();
            } else if (v.getId() == R.id.app_video_finish) {
                if (!player.onBackPressed()) {
                    ((Activity) videoView.getContext()).finish();
                }
            } else if (v.getId() == R.id.app_video_float_close) {
                player.stop();
                player.setDisplayModel(StreamFiillPlayer.DISPLAY_NORMAL);
            } else if (v.getId() == R.id.app_video_float_full) {
                player.setDisplayModel(StreamFiillPlayer.DISPLAY_FULL_WINDOW);
            } else if (v.getId() == R.id.app_video_clarity) {
                Activity activity = (Activity) videoView.getContext();
                if (activity instanceof AppCompatActivity) {
                    TrackSelectorFragment trackSelectorFragment = new TrackSelectorFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("fingerprint", videoView.getVideoInfo().getFingerprint());
                    trackSelectorFragment.setArguments(bundle);
                    FragmentManager supportFragmentManager = ((AppCompatActivity) activity).getSupportFragmentManager();
                    trackSelectorFragment.show(supportFragmentManager, "player_track");
                }

            }
        }
    };

    protected void initView(View view) {
        seekBar = viewquery.id(R.id.app_video_seekBar).view();
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(seekListener);
        viewquery.id(R.id.app_video_play).clicked(onClickListener).imageView().setRotation(isRtl()?180:0);
        viewquery.id(R.id.app_video_fullscreen).clicked(onClickListener);
        viewquery.id(R.id.app_video_finish).clicked(onClickListener).imageView().setRotation(isRtl()?180:0);
        viewquery.id(R.id.app_video_replay_icon).clicked(onClickListener).imageView().setRotation(isRtl()?180:0);
        viewquery.id(R.id.app_video_clarity).clicked(onClickListener);
        viewquery.id(R.id.app_video_float_close).clicked(onClickListener);
        viewquery.id(R.id.app_video_float_full).clicked(onClickListener);


        final GestureDetector gestureDetector = new GestureDetector(context, createGestureListener());
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (displayModel == StreamFiillPlayer.DISPLAY_FLOAT) {
                    return false;
                }

                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        endGesture();
                        break;
                }
                return true;
            }
        });
    }

    protected GestureDetector.OnGestureListener createGestureListener() {
        return new PlayerGestureListener();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_FADE_OUT:
                hide(false);
                break;
            case MESSAGE_HIDE_CENTER_BOX:
                viewquery.id(R.id.app_video_volume_box).gone();
                viewquery.id(R.id.app_video_brightness_box).gone();
                viewquery.id(R.id.app_video_fastForward_box).gone();
                break;
            case MESSAGE_SEEK_NEW_POSITION:
                if (newPosition >= 0) {
                    videoView.getPlayer().seekTo((int) newPosition);
                    newPosition = -1;
                }
                break;
            case MESSAGE_SHOW_PROGRESS:
                setProgress();
                if (!isDragging && isShowing) {
                    msg = handler.obtainMessage(MESSAGE_SHOW_PROGRESS);
                    handler.sendMessageDelayed(msg, 300);
                    updatePausePlay();
                }
                break;
            case MESSAGE_RESTART_PLAY:
//                        play(url);
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(StreamFiillPlayer fiillPlayer) {
        statusChange(STATUS_COMPLETED);
    }

    @Override
    public void onRelease(StreamFiillPlayer fiillplayer) {
        handler.removeCallbacksAndMessages(null);

        viewquery.id(R.id.app_video_play).image(R.drawable.ic_play_arrow_white_24dp);
        viewquery.id(R.id.app_video_currentTime).text("");
        viewquery.id(R.id.app_video_endTime).text("");

        //1.set the cover view visible
        viewquery.id(R.id.app_video_cover).visible();
        //2.set current view as cover
        VideoInfo videoInfo = videoView.getVideoInfo();
        if (videoInfo.isCurrentVideoAsCover()) {
            if (fiillplayer.getCurrentState() != StreamFiillPlayer.STATE_ERROR) {
                ScalableTextureView currentDisplay = fiillplayer.getCurrentDisplay();
                if (currentDisplay != null) {
                    ImageView imageView = viewquery.id(R.id.app_video_cover).imageView();
                    if (imageView != null) {
                        int aspectRatio = videoInfo.getAspectRatio();
                        if (aspectRatio == VideoInfo.AR_ASPECT_FILL_PARENT) {
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else if (aspectRatio == VideoInfo.AR_MATCH_PARENT) {
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        } else if (aspectRatio == VideoInfo.AR_ASPECT_WRAP_CONTENT) {
                            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        } else {
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }
                        imageView.setImageBitmap(currentDisplay.getBitmap());
                    }
                }
            }
        }

    }

    @Override
    public void onStart(StreamFiillPlayer fiillPlayer) {
        viewquery.id(R.id.app_video_replay).gone();
        show(defaultTimeout);
        statusChange(STATUS_PLAYING);
    }


    protected void endGesture() {
        volume = -1;
        brightness = -1f;
        if (newPosition >= 0) {
            handler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
            handler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
        }
        handler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Toast.makeText(context, "onDoubleTap", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return true;

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //1. if not the active player,ignore
            boolean currentPlayer = videoView.isCurrentActivePlayer();
            if (!currentPlayer) {
                return true;
            }

            float oldX = e1.getX(), oldY = e1.getY();
            float deltaY = oldY - e2.getY();
            float deltaX = oldX - e2.getX();
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                volumeControl = oldX > videoView.getWidth() * 0.5f;
                firstTouch = false;
            }
            StreamFiillPlayer player = videoView.getPlayer();
            if (toSeek) {
                if (player.canSeekForward()) {
                    onProgressSlide(-deltaX / videoView.getWidth());
                }
            } else {
                //if player in list controllerView,ignore
                if (displayModel == StreamFiillPlayer.DISPLAY_NORMAL && videoView.inListView()) {
                    return true;
                }
                float percent = deltaY / videoView.getHeight();
                if (volumeControl) {
                    onVolumeSlide(percent);
                } else {
                    onBrightnessSlide(percent);
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isShowing) {
                hide(false);
            } else {
                show(defaultTimeout);
            }
            return true;
        }
    }

    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        hide(true);

        int index = (int) (percent * maxVolume) + volume;
        if (index > maxVolume)
            index = maxVolume;
        else if (index < 0)
            index = 0;

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        int i = (int) (index * 1.0 / maxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }

        viewquery.id(R.id.app_video_volume_icon).image(i == 0 ? R.drawable.ic_volume_off_white_36dp : R.drawable.ic_volume_up_white_36dp);
        viewquery.id(R.id.app_video_brightness_box).gone();
        viewquery.id(R.id.app_video_volume_box).visible();
        viewquery.id(R.id.app_video_volume_box).visible();
        viewquery.id(R.id.app_video_volume).text(s).visible();
    }

    private void onProgressSlide(float percent) {
        StreamFiillPlayer player = videoView.getPlayer();
        long position = player.getCurrentPosition();
        long duration = player.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);
        if (isRtl()) {
            delta = -1 * delta;
        }

        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            viewquery.id(R.id.app_video_fastForward_box).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            viewquery.id(R.id.app_video_fastForward).text(text + "s");
            viewquery.id(R.id.app_video_fastForward_target).text(generateTime(newPosition) + "/");
            viewquery.id(R.id.app_video_fastForward_all).text(generateTime(duration));
        }
        handler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
    }

    private void onBrightnessSlide(float percent) {
        Window window = ((Activity) context).getWindow();
        if (brightness < 0) {
            brightness = window.getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        Log.d(this.getClass().getSimpleName(), "brightness:" + brightness + ",percent:" + percent);
        viewquery.id(R.id.app_video_brightness_box).visible();
        WindowManager.LayoutParams lpa = window.getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        viewquery.id(R.id.app_video_brightness).text(((int) (lpa.screenBrightness * 100)) + "%");
        window.setAttributes(lpa);

    }

    @Override
    public boolean onInfo(StreamFiillPlayer fiillplayer, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                statusChange(STATUS_LOADING);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                statusChange(STATUS_PLAYING);
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                //Toaster.show("download rate:" + extra);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                statusChange(STATUS_PLAYING);
                break;

            default:
        }

        return true;
    }

    @Override
    public void onCurrentStateChange(int oldState, int newState) {
        if (context instanceof Activity) {
            if (newState == StreamFiillPlayer.STATE_LAZYLOADING) {
                viewquery.id(R.id.app_video_loading).gone();
                viewquery.id(R.id.app_video_status).visible()
                        .id(R.id.app_video_status_text)
                        .text(context.getString(R.string.fiill_player_lazy_loading, 0));
            }
            if (newState == StreamFiillPlayer.STATE_PLAYING) {
                //set SCREEN_ON
                ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    protected void statusChange(int status) {
        this.status = status;

        switch (status) {
            case STATUS_LOADING:
                viewquery.id(R.id.app_video_loading).visible();
                viewquery.id(R.id.app_video_status).gone();
                break;
            case STATUS_PLAYING:
                viewquery.id(R.id.app_video_loading).gone();
                viewquery.id(R.id.app_video_status).gone();
                break;
            case STATUS_COMPLETED:
                handler.removeMessages(MESSAGE_SHOW_PROGRESS);
                showBottomControl(false);
                viewquery.id(R.id.app_video_replay).visible();
                viewquery.id(R.id.app_video_loading).gone();
                viewquery.id(R.id.app_video_status).gone();
                break;
            case STATUS_ERROR:
                viewquery.id(R.id.app_video_status).visible().id(R.id.app_video_status_text).text(R.string.small_problem);
                handler.removeMessages(MESSAGE_SHOW_PROGRESS);
                viewquery.id(R.id.app_video_loading).gone();
                break;
            default:
        }
    }

    @Override
    public boolean onError(StreamFiillPlayer fiillplayer, int what, int extra) {
        statusChange(STATUS_ERROR);
        return true;
    }

    @Override
    public void onPrepared(StreamFiillPlayer fiillplayer) {
        boolean live = fiillplayer.getDuration() == 0;
        viewquery.id(R.id.app_video_seekBar).enabled(!live);
        if (fiillplayer.getTrackInfo().length > 0) {
            viewquery.id(R.id.app_video_clarity).visible();
        } else {
            viewquery.id(R.id.app_video_clarity).gone();
        }
    }

    @Override
    public void onPreparing(StreamFiillPlayer fiillPlayer) {
        statusChange(STATUS_LOADING);
    }

    @Override
    public void onDisplayModelChange(int oldModel, int newModel) {
        this.displayModel = newModel;
        if (displayModel == StreamFiillPlayer.DISPLAY_FLOAT) {
            viewquery.id(R.id.app_video_float_close).visible();
            viewquery.id(R.id.app_video_float_full).visible();
            viewquery.id(R.id.app_video_bottom_box).gone();

        } else {
            viewquery.id(R.id.app_video_float_close).gone();
            viewquery.id(R.id.app_video_float_full).gone();
            viewquery.id(R.id.app_video_bottom_box).visible();

        }
    }

    @Override
    public void onTargetStateChange(int oldState, int newState) {
        if (newState != StreamFiillPlayer.STATE_IDLE) {
            viewquery.id(R.id.app_video_cover).gone();
        }
    }


    @Override
    public void onTimedText(StreamFiillPlayer fiillPlayer, IjkTimedText text) {
        if (text == null) {
            viewquery.id(R.id.app_video_subtitle).gone();
        } else {
            viewquery.id(R.id.app_video_subtitle).visible().text(text.getText());
        }
    }

    @Override
    public void onLazyLoadProgress(StreamFiillPlayer fiillPlayer, int progress) {
        viewquery.id(R.id.app_video_loading).gone();
        viewquery.id(R.id.app_video_status).visible();
        viewquery.id(R.id.app_video_status_text)
                .text(context.getString(R.string.fiill_player_lazy_loading, progress));
    }

    @Override
    public void onLazyLoadError(StreamFiillPlayer fiillPlayer, String message) {
        viewquery.id(R.id.app_video_loading).gone();
        viewquery.id(R.id.app_video_status).visible();
        viewquery.id(R.id.app_video_status_text)
                .text(context.getString(R.string.fiill_player_lazy_loading_error, message));
    }

    @Override
    public void onPause(StreamFiillPlayer fiillPlayer) {
        statusChange(STATUS_PAUSE);
    }

    private boolean isRtl() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(StreamFiillPlayer fiillplayer, int percent) {
    }

    @Override
    public void onSeekComplete(StreamFiillPlayer fiillplayer) {

    }
}
