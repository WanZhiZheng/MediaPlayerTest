package com.example.mediaplayertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class VideoViewActivity extends AppCompatActivity implements View.OnClickListener {
    VideoView mVideoView;
    Button btn_play,btn_pause,btn_stop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        btn_play=(Button)findViewById(R.id.videoView_play);
        btn_pause=(Button)findViewById(R.id.videoView_pause);
        btn_stop=(Button)findViewById(R.id.videoView_stop);
        mVideoView=(VideoView)findViewById(R.id.video);
        mVideoView.setVideoPath("/sdcard/Movies/1.mp4");
        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.videoView_play:
                if(!mVideoView.isPlaying())
                    mVideoView.start();
                break;
            case R.id.videoView_pause:
                mVideoView.pause();
                break;
            case R.id.videoView_stop:
                mVideoView.resume();
                break;
            default:
                break;
        }
    }
}
