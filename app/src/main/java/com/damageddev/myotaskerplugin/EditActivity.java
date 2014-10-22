package com.damageddev.myotaskerplugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.damageddev.myotaskerplugin.services.BackgroundService;
import com.damageddev.myotaskerplugin.utils.Constants;
import com.thalmic.myo.Pose;


public class EditActivity extends ActionBarActivity {
    private ListView mListView;
    private GestureListAdapter mAdapter;

    private Gesture mSelectedGesture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new GestureListAdapter();

        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_edit);

        setSupportActionBar(toolbar);

        startService(new Intent(this, BackgroundService.class));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mSelectedGesture = Gesture.values()[position];
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_done) {
                    onBackPressed();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    private class GestureListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return Gesture.values().length;
        }

        @Override
        public Object getItem(int position) {
            return Gesture.values()[position];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gesture_selection, null);

                viewHolder = new ViewHolder();
                viewHolder.gestureIconImageView = (ImageView) convertView.findViewById(R.id.gesture_icon_image_view);
                viewHolder.gestureNameTextView = (TextView) convertView.findViewById(R.id.gesture_name_text_view);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Gesture gesture = Gesture.values()[position];

            viewHolder.gestureIconImageView.setImageResource(gesture.getIconResourceId());
            viewHolder.gestureNameTextView.setText(gesture.getLabelResourceId());

            return convertView;
        }

        private class ViewHolder {
            ImageView gestureIconImageView;
            TextView gestureNameTextView;
        }
    }

    @Override
    public void onBackPressed() {
        if (mSelectedGesture == null) {
            Toast.makeText(this, getString(R.string.must_select_pose), Toast.LENGTH_LONG).show();
            return;
        }

        finish();
    }

    @Override
    public void finish() {


        Intent resultIntent = new Intent();

        Bundle result = new Bundle();
        result.putString(Constants.POSE, mSelectedGesture.getPose().toString());

        resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, result);
        resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, getString(mSelectedGesture.getLabelResourceId()));

        setResult(RESULT_OK, resultIntent);

        super.finish();
    }

    private enum Gesture {
        Fist(Pose.FIST, R.drawable.solid_blue_fist, R.string.fist),
        ThumbToPinky(Pose.THUMB_TO_PINKY, R.drawable.solid_blue_pinky_thumb, R.string.thumb_to_pinky),
        WaveIn(Pose.WAVE_IN, R.drawable.solid_blue_wave_left, R.string.wave_in),
        WaveOut(Pose.WAVE_OUT, R.drawable.solid_blue_wave_right, R.string.wave_out),
        FingersSpread(Pose.FINGERS_SPREAD, R.drawable.solid_blue_spread_fingers, R.string.fingers_spread);

        Pose mPose;
        int mIconResourceId;
        int mLabelResourceId;

        Gesture(Pose pose, int iconResourceId, int labelResourceId) {
            mPose = pose;
            mIconResourceId = iconResourceId;
            mLabelResourceId = labelResourceId;
        }

        Pose getPose() {
            return mPose;
        }

        int getLabelResourceId() {
            return mLabelResourceId;
        }

        int getIconResourceId() {
            return mIconResourceId;
        }
    }
}
