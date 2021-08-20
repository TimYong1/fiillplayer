package com.fiill.fiillplayer.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fiill.fiillplayer.R;
import com.fiill.fiillplayer.streamplayer.VideoInfo;
import com.fiill.fiillplayer.streamplayer.PlayerManager;
import com.fiill.fiillplayer.streamplayer.VideoView;

/**
 * This activity is for playing streams, a container for fiillplayer
 */

public class StreamPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fiill_player_activity);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        VideoInfo videoInfo = intent.getParcelableExtra("__video_info__");
        if (videoInfo == null) {
            finish();
            return;
        }
        if(videoInfo.isFullScreenOnly()){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        PlayerManager.getInstance().releaseByFingerprint(videoInfo.getFingerprint());
        VideoView videoView = findViewById(R.id.video_view);

        videoView.videoInfo(videoInfo);
        PlayerManager.getInstance().getPlayer(videoView).start();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PlayerManager.getInstance().onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (PlayerManager.getInstance().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
