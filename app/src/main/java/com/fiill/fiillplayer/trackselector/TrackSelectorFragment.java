package com.fiill.fiillplayer.trackselector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ExpandableListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fiill.fiillplayer.R;
import com.fiill.fiillplayer.streamplayer.ViewQuery;

/**
 * This Fragment will show when user press settings key in video view
 * to choose audio/video/text track when live stream
 */

public class TrackSelectorFragment extends DialogFragment {
    private ViewQuery $;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fiill_track_selector, container, false);
        $ = new ViewQuery(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ExpandableListView list = $.id(R.id.app_video_track_list).view();

        $.id(R.id.app_video_track_close).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
//                getFragmentManager().beginTransaction().remove(TrackSelectorFragment.this).commit();
            }
        });

        final TracksAdapter tracksAdapter = new TracksAdapter();
        list.setGroupIndicator(null);
        list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        list.setAdapter(tracksAdapter);
        tracksAdapter.load(getArguments().getString("fingerprint"));
        int count = tracksAdapter.getGroupCount();
        for ( int i = 0; i < count; i++ ) {
            list.expandGroup(i);
        }
    }
}
