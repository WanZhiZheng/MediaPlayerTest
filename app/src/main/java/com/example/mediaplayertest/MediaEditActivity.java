package com.example.mediaplayertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MediaEditActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_beginTime,et_endTime;
    private Button btn_edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_edit);
        et_beginTime = (EditText)findViewById(R.id.et_begintime);
        et_endTime = (EditText)findViewById(R.id.et_endtime);
        btn_edit = (Button)findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(this);




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_edit:
                cutVideo(et_beginTime.getText().toString(),et_endTime.getText().toString());
                break;
        }
    }

    private void cutVideo(String begin, String end){
        int begin_,end_;
        try{
            begin_ = Integer.parseInt(begin);
            end_ = Integer.parseInt(end);
        }catch (NumberFormatException e){
            Toast.makeText(MediaEditActivity.this,"请输入正确的剪切时间",Toast.LENGTH_SHORT);
            return ;
        }
        MediaEditor mMediaEditor = new MediaEditor();
        mMediaEditor.decodeVAFormatToMuxer();
        mMediaEditor.process_cut(begin_,end_,1.0);
        Toast.makeText(MediaEditActivity.this,"剪辑完成,sdcard/Movies/result.mp4",Toast.LENGTH_SHORT);
    }
}
