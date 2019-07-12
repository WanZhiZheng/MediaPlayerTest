package com.example.mediaplayertest;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Button btn_play,btn_pause,btn_stop,btn_play2,btn_pause2,btn_stop2;
    private MediaPlayer mPlayer;
    private  MediaPlayer mPlayer2;
    private SeekBar mSeekBar,mSeekBar2;
    boolean isPrepare,isPrepare2;
    private TextView txt_current,txt_end,txt_current2,txt_end2;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

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
        setContentView(R.layout.activity_main);
        {
            btn_play = (Button) findViewById(R.id.play);
            btn_pause = (Button) findViewById(R.id.pause);
            btn_stop = (Button) findViewById(R.id.stop);
            btn_play2 = (Button) findViewById(R.id.play2);
            btn_pause2 = (Button) findViewById(R.id.pause2);
            btn_stop2 = (Button) findViewById(R.id.stop2);
            mSeekBar = (SeekBar) findViewById(R.id.seekbar);
            mSeekBar2 = (SeekBar) findViewById(R.id.seekbar2);
            txt_end = (TextView) findViewById(R.id.tv2);
            txt_current = (TextView) findViewById(R.id.tv);
            txt_end2 = (TextView) findViewById(R.id.tv4);
            txt_current2 = (TextView) findViewById(R.id.tv3);
            mSeekBar.setOnSeekBarChangeListener(this);
            mSeekBar2.setOnSeekBarChangeListener(this);
            btn_play.setOnClickListener(this);
            btn_pause.setOnClickListener(this);
            btn_stop.setOnClickListener(this);
            btn_play2.setOnClickListener(this);
            btn_pause2.setOnClickListener(this);
            btn_stop2.setOnClickListener(this);
            mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        }



        try {
            initMusicPlayer();
            //initVideoPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4


    private void initMusicPlayer() throws IOException {
        mPlayer = new MediaPlayer();
        AssetFileDescriptor fileDescriptor = getAssets().openFd("happier.mp3");

        mPlayer.setDataSource(fileDescriptor.getFileDescriptor());

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                isPrepare = true;
            }
        });
        mPlayer.prepareAsync();
        //mPlayer.getCurrentPosition();
    }

    private void initVideoPlayer() throws IOException {
        mPlayer = new MediaPlayer();
        //AssetFileDescriptor fileDescriptor = getAssets().openFd("happier.mp3");

        mPlayer.setDataSource("/sdcard/Movies/1.mp4");
        mPlayer.setDisplay(mSurfaceView.getHolder());

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                isPrepare = true;
            }
        });
        mPlayer.prepareAsync();
        //mPlayer.getCurrentPosition();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.play:
                if(!mPlayer.isPlaying() && isPrepare){
                    mPlayer.start();
                    int totalTime = Math.round(mPlayer.getDuration()/1000);
                    String str_totalTime = String.format("%02d:%02d",totalTime/60,totalTime%60);
                    txt_end.setText(str_totalTime);
                    mSeekBar.setMax(mPlayer.getDuration());
                    //mSeekBar.setMax(totalTime);  how about this
                    mHandler.postDelayed(runnable,1000);

                }
                break;
            case R.id.pause:
                mPlayer.pause();
                break;
            case R.id.stop:
                mPlayer.stop();
                try {
                    mPlayer.reset();
                    initMusicPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;


            case R.id.play2:
                try {
                    //initMusicPlayer();
                    initVideoPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;

            case R.id.pause2:
                try {
                    initMusicPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
//            case R.id.stop2:
//
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.seekbar:
                if(mPlayer!=null && fromUser)
                    mPlayer.seekTo(seekBar.getProgress());
                break;
//            case R.id.seekbar2:
//                if(mPlayer2!=null && fromUser)
//                    mPlayer2.seekTo(mSeekBar2.getProgress());
//                break;
        }


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
