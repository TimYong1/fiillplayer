package com.fiill.fiillplayer.trackselector;

import com.fiill.fiillplayer.R;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 *  
 */

public class TrackGroup {
    private final int trackType;

    public int getSelectedTrackIndex() {
        return selectedTrackIndex;
    }

    public void setSelectedTrackIndex(int selectedTrackIndex) {
        this.selectedTrackIndex = selectedTrackIndex;
    }

    private int selectedTrackIndex= -1;

    public int getTrackType() {
        return trackType;
    }


    public TrackGroup(int trackType,int selectedTrackIndex) {
        this.trackType = trackType;
        this.selectedTrackIndex = selectedTrackIndex;
    }

    public List<TrackInfoWrapper> getTracks() {
        return tracks;
    }

    private final List<TrackInfoWrapper> tracks = new ArrayList<>();

    public int getTrackTypeName() {
        if (trackType==ITrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
            return R.string.fiill_player_track_type_audio;
        } else if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
            return R.string.fiill_player_track_type_video;
        } else if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
            return R.string.fiill_player_track_type_timed_text;
        } else if (trackType == ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
            return R.string.fiill_player_track_type_subtitle;
        } else {
            return R.string.fiill_player_track_type_unknown;

        }
    }
}
