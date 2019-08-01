package com.example.mediaplayertest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button btn_SurfaceView, btn_MusicPlayer, btn_VideoView,btn_ModifiedSurfaceView,btn_TextureView,btn_MediaEdit,btn_Decode;
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            btn_SurfaceView = (Button)findViewById(R.id.SurfaceView);
            btn_MusicPlayer = (Button)findViewById(R.id.MusicPlayer);
            btn_VideoView = (Button)findViewById(R.id.VideoView);
            btn_ModifiedSurfaceView = (Button)findViewById(R.id.modifiedsurfaceview);
            btn_TextureView=(Button)findViewById(R.id.textureView);
            btn_Decode=(Button)findViewById(R.id.decodeTest);
            btn_MediaEdit=(Button)findViewById(R.id.media_edit);
            btn_ModifiedSurfaceView.setOnClickListener(this);
            btn_TextureView.setOnClickListener(this);
            btn_SurfaceView.setOnClickListener(this);
            btn_MusicPlayer.setOnClickListener(this);
            btn_VideoView.setOnClickListener(this);
            btn_Decode.setOnClickListener(this);
            btn_MediaEdit.setOnClickListener(this);
            

    }



    //http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4



    @Override
    public void onClick(View view) {

        switch (view.getId()){
            
            case R.id.SurfaceView:
                Intent intent3=new Intent(MainActivity.this,SurfaceViewActivity.class);
                startActivity(intent3);
                break;

            case R.id.MusicPlayer:
                Intent intent2=new Intent(MainActivity.this,MusicPlayer.class);
                startActivity(intent2);
                break;
                
            case R.id.VideoView:
                Intent intent=new Intent(MainActivity.this, VideoViewActivity.class);
                startActivity(intent);
                break;

            case R.id.modifiedsurfaceview:
                Intent intent1=new Intent(MainActivity.this,ModifiedSurfaceViewActivity.class);
                startActivity(intent1);

                break;

            case R.id.textureView:
                Intent intent4=new Intent(MainActivity.this,TextureViewActivity.class);
                startActivity(intent4);
                break;
            case R.id.decodeTest:
                Intent intent5=new Intent(MainActivity.this,DecodeTestActivity.class);
                startActivity(intent5);

                break;
            case R.id.media_edit:
                Intent intent6=new Intent(MainActivity.this,MediaEditActivity.class);
                startActivity(intent6);

        }
    }

    
}
