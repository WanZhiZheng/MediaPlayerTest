package com.example.mediaplayertest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class MusicPlayer extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private Button btn_play,btn_pause,btn_stop;
    private MediaPlayer mPlayer;
    private SeekBar mSeekBar;
    private TextView txt_current,txt_end;
   // boolean isPrepare;


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
        setContentView(R.layout.activity_music_player);
        btn_play = (Button) findViewById(R.id.play);
        mPlayer=new MediaPlayer();
       // btn_play.setEnabled(false);
        btn_pause = (Button) findViewById(R.id.pause);
        btn_stop = (Button) findViewById(R.id.stop);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        txt_end = (TextView) findViewById(R.id.tv2);
        txt_current = (TextView) findViewById(R.id.tv);
        mSeekBar.setOnSeekBarChangeListener(this);
        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);


    }

    private void initMusicPlayer() throws IOException {
        //mPlayer = new MediaPlayer();
        AssetFileDescriptor fileDescriptor = getAssets().openFd("happier.mp3");

        mPlayer.setDataSource(fileDescriptor.getFileDescriptor());
        //mPlayer.prepare();
        //mPlayer.start();

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
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.play:
                if(!mPlayer.isPlaying() ){
                    try {
                        initMusicPlayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    int totalTime = Math.round(mPlayer.getDuration()/1000);
//                    String str_totalTime = String.format("%02d:%02d",totalTime/60,totalTime%60);
//                    txt_end.setText(str_totalTime);
//                    mSeekBar.setMax(mPlayer.getDuration());
//                    //mSeekBar.setMax(totalTime);  how about this
//                    mHandler.postDelayed(runnable,1000);

            }
                break;
            case R.id.pause:
                mPlayer.pause();
                break;
            case R.id.stop:
                mPlayer.stop();
                mPlayer.reset();

                break;

        }
    }
}
