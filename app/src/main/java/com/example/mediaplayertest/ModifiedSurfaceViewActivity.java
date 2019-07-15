package com.example.mediaplayertest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class ModifiedSurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnVideoSizeChangedListener {
    private Button btn_play,btn_pause,btn_stop;
    private MediaPlayer mPlayer;
    private SeekBar mSeekBar;
    private SurfaceView mSurfaceView;
    private TextView txt_current,txt_end;
    private int surfaceWidth,surfaceHeight;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);


        }
    };

    Runnable runnable=new Runnable() {
        @Override
        public void run() {

            mHandler.postDelayed(this,1000);
            int currentTime=Math.round(mPlayer.getCurrentPosition() / 1000);
            String str_currTime=String.format("%02d:%02d",currentTime/60,currentTime%60);
            txt_current.setText(str_currTime);
            mSeekBar.setProgress(mPlayer.getCurrentPosition());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modified_surface_view);
        mPlayer=new MediaPlayer();
        btn_play = (Button) findViewById(R.id.play2);
        btn_pause = (Button) findViewById(R.id.pause2);
        btn_stop = (Button) findViewById(R.id.stop2);
        txt_end = (TextView) findViewById(R.id.tv4);
        txt_current = (TextView) findViewById(R.id.tv3);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar2);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);
        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);




    }

    private void initVideoPlayer() throws IOException {
        //mPlayer = new MediaPlayer();
        //AssetFileDescriptor fileDescriptor = getAssets().openFd("happier.mp3");

        mPlayer.setDataSource("/sdcard/Movies/1.mp4");
        //mPlayer.setDisplay(mSurfaceView.getHolder());
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayer.start();
                int totalTime = Math.round(mPlayer.getDuration()/1000);
                String str_totalTime = String.format("%02d:%02d",totalTime/60,totalTime%60);
                txt_end.setText(str_totalTime);
                mSeekBar.setMax(mPlayer.getDuration());
                //mSeekBar.setMax(totalTime);  how about this
                mHandler.postDelayed(runnable,1000);
            }
        });
        mPlayer.prepareAsync();
        //mPlayer.getCurrentPosition();
    }

    public void changeVideoSize() {

        int videoWidth = mPlayer.getVideoWidth();

        int videoHeight = mPlayer.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (getResources().getConfiguration().orientation== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth,(float) videoHeight / (float) surfaceHeight);
        } else{
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth/(float) surfaceHeight),(float) videoHeight/(float) surfaceWidth);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
       // videoWidth=(int)Math.ceil()
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoWidth, videoHeight));
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mPlayer.setOnVideoSizeChangedListener(this);
        mPlayer.setDisplay(mSurfaceView.getHolder());
        if(getResources().getConfiguration().orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            surfaceWidth=mSurfaceView.getWidth();
            surfaceHeight=mSurfaceView.getHeight();
        }else {
            surfaceWidth=mSurfaceView.getHeight();
            surfaceHeight=mSurfaceView.getWidth();
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play2:
                if(!mPlayer.isPlaying()){
                    try {
                        initVideoPlayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case R.id.pause2:
                mPlayer.pause();
                break;
            case R.id.stop2:
                mPlayer.stop();
                mPlayer.reset();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(mPlayer!=null && fromUser)
            mPlayer.seekTo(seekBar.getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeVideoSize();

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

        changeVideoSize();
    }
}