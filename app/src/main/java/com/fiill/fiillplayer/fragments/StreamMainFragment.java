package com.fiill.fiillplayer.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fiill.fiillplayer.R;
import com.fiill.fiillplayer.streamplayer.ViewQuery;
import com.fiill.fiillplayer.streamplayer.StreamFiillPlayer;
import com.fiill.fiillplayer.streamplayer.Option;
import com.fiill.fiillplayer.streamplayer.PlayerManager;
import com.fiill.fiillplayer.streamplayer.VideoInfo;
import com.fiill.fiillplayer.streamplayer.VideoView;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * This is the main Fragment of stream mode activities.
 */

public class StreamMainFragment extends Fragment {
    private ViewQuery $;
    private int aspectRatio = VideoInfo.AR_ASPECT_FIT_PARENT;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerManager.getInstance().getDefaultVideoInfo().addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "multiple_requests", 1L));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stream_fragment_main, container, false);
    }

    protected void hideInput(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != view) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        $ = new ViewQuery(view);

        final VideoView videoView = $.id(R.id.video_view).view();
        CheckBox cb = $.id(R.id.cb_pwf).view();
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                videoView.getVideoInfo().setPortraitWhenFullScreen(isChecked);
            }
        });

        RadioGroup rb = $.id(R.id.rg_ra).view();
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                hideInput(view);
                if (checkedId == R.id.rb_4_3) {
                    aspectRatio = VideoInfo.AR_4_3_FIT_PARENT;
                } else if (checkedId == R.id.rb_16_9) {
                    aspectRatio = VideoInfo.AR_16_9_FIT_PARENT;
                } else if (checkedId == R.id.rb_fill_parent) {
                    aspectRatio = VideoInfo.AR_ASPECT_FILL_PARENT;
                } else if (checkedId == R.id.rb_wrap_content) {
                    aspectRatio = VideoInfo.AR_ASPECT_WRAP_CONTENT;
                } else if (checkedId == R.id.rb_match_parent) {
                    aspectRatio = VideoInfo.AR_MATCH_PARENT;
                } else if(checkedId == R.id.rb_fit_parent) {
					aspectRatio = VideoInfo.AR_ASPECT_FIT_PARENT;
                }
                StreamFiillPlayer player;
                try {
                    player = videoView.getPlayer();
                    player.aspectRatio(aspectRatio);
                }catch (RuntimeException ignored) {
                    ;
                }
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInput(v);
                String url =  ((EditText)getActivity().
                        findViewById(R.id.et_url)).getText().toString();
                videoView.setVideoPath(url);
				if (v.getId() == R.id.img_back) {
					getActivity().finish();
                }  else if (v.getId() == R.id.btn_play) {
                    StreamFiillPlayer player = videoView.getPlayer();
                    if (null != player && player.isPlaying()) {
                        player.pause();
                    } else if (null != player){
                        player.start();
                    }
                } else if (v.getId() == R.id.btn_full) {
                    videoView.getPlayer().toggleFullScreen();
                } else if (v.getId() == R.id.btn_play_float) {
                    videoView.getPlayer().setDisplayModel(StreamFiillPlayer.DISPLAY_FLOAT);
                } else if (v.getId() == R.id.btn_play_in_standalone) {
                    VideoInfo videoInfo = new VideoInfo(Uri.parse($.id(R.id.et_url).text()))
                            .setTitle(url)
                            .setAspectRatio(aspectRatio)
                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1L))
                            .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "multiple_requests", 1L))

                            .setShowTopBar(true);
                    StreamFiillPlayer.debug = true;//show java logs
                    StreamFiillPlayer.play(getContext(), videoInfo);
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        };
        $.id(R.id.img_back).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_play).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_play_float).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_full).view().setOnClickListener(onClickListener);
        $.id(R.id.btn_play_in_standalone).view().setOnClickListener(onClickListener);
    }


}
