package com.example.mediaplayertest;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Button btn_play,btn_pause,btn_stop;
    private MediaPlayer player;
    private SeekBar mSeekBar;
    private TextView txt_current,txt_end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_play=(Button)findViewById(R.id.play);
        btn_pause=(Button)findViewById(R.id.pause);
        btn_stop=(Button)findViewById(R.id.stop);
        mSeekBar=(SeekBar)findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        player=new MediaPlayer();
        initMediaPlayer();


    }


    private void initMediaPlayer(){


            player=MediaPlayer.create(this,R.raw.happier);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.play:
                if(!player.isPlaying()){
                    player.start();
                    Log.d("hello", String.valueOf(player.getDuration()));
                }

        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
