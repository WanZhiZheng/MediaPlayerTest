package com.example.mediaplayertest;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.TreeMap;

public class DecodeTestActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_play,btn_pause,btn_stop;
    Surface surface;
    String TAG="hello";
    SurfaceView mSurfaceView;
    VideoThread videoThread;
    AudioThread audioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_test);
        btn_play = (Button)findViewById(R.id.play_decode);
        btn_pause = (Button)findViewById(R.id.pause_decode);
        btn_stop = (Button)findViewById(R.id.stop_decode);
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView_decode);
        surface=mSurfaceView.getHolder().getSurface();
        btn_play.setOnClickListener(this);

        videoThread = new VideoThread();
        audioThread = new AudioThread();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_decode:
                videoThread.start();
                audioThread.start();
                Log.d(TAG, "onClick: ");
                break;
            case R.id.pause_decode:

                break;
        }
    }


    private class VideoThread extends Thread{

        @Override
        public void run() {
            super.run();

            MediaExtractor videoExtractor = new MediaExtractor();

            MediaCodec videoCodec = null;
            try {
                videoExtractor.setDataSource("/sdcard/Movies/1.mp4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            int videoTrackIndex = getMediaTrackIndex(videoExtractor, "video/");


            MediaFormat videoFormat = videoExtractor.getTrackFormat(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);
            try {
                videoCodec = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
                videoCodec.configure(videoFormat,surface,null,0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            videoCodec.start();

            MediaCodec.BufferInfo viedoBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] InputBuffers = videoCodec.getInputBuffers();

            boolean isVideoEOS = false;
            long startMs=System.currentTimeMillis();
            while(!Thread.interrupted()){

                if(!isVideoEOS){
                    isVideoEOS = putBufferToDecoder(videoCodec, videoExtractor, InputBuffers);
                }

                int outputIndex = videoCodec.dequeueOutputBuffer(viedoBufferInfo,1000);
                Log.i(TAG, "outputIndex is " + outputIndex);

                switch (outputIndex){
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.v(TAG, "format changed");
                    break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.v(TAG, "超时");
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        //outputBuffers = videoCodec.getOutputBuffers();
                        Log.v(TAG, "output buffers changed");
                        break;
                    default:
                        //直接渲染到Surface时使用不到outputBuffer
                        //ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        //延时操作
                        //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                        //sleepRender(videoBufferInfo, startMs);
                        //渲染
                        sleepRender(viedoBufferInfo,startMs);
                        videoCodec.releaseOutputBuffer(outputIndex, true);
                        break;
                }
                if((viedoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    Log.v(TAG, "EOS");
                    break;
                }

            }

        }
    }


    private class AudioThread extends Thread{
        private int audioInputBufferSize;
        private  AudioTrack audioTrack;
        @Override
        public void run() {
            super.run();

            MediaExtractor audioExtractor = new MediaExtractor();
            MediaCodec audioCodec = null;
            try {
                audioExtractor.setDataSource("/sdcard/Movies/1.mp4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            int TrackIndex=-1;
            for(int i=0;i<audioExtractor.getTrackCount();i++){
                if(audioExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME).startsWith("audio/")){
                    TrackIndex = i;
                    break;
                }
            }
            MediaFormat audioFormat = audioExtractor.getTrackFormat(TrackIndex);
            audioExtractor.selectTrack(TrackIndex);
            int audioChannels = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            int audioSampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int minBufferSize = AudioTrack.getMinBufferSize(audioChannels,(audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),AudioFormat.ENCODING_PCM_16BIT);
//            int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
//                    (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
//                    AudioFormat.ENCODING_PCM_16BIT);
            int maxInputSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            audioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
            int frameSizeInBytes = audioChannels * 2;
            audioInputBufferSize = (audioInputBufferSize / frameSizeInBytes) * frameSizeInBytes;

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    audioSampleRate,
                    (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioInputBufferSize,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();


            try {
                audioCodec=MediaCodec.createDecoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
                audioCodec.configure(audioFormat,null,null,0);
            } catch (IOException e) {
                e.printStackTrace();
            }
                audioCodec.start();

            final ByteBuffer[] buffers = audioCodec.getOutputBuffers();
            int sz = buffers[0].capacity();
            if (sz <= 0)
                sz = audioInputBufferSize;
            byte[] mAudioOutTempBuf = new byte[sz];

            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = audioCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = audioCodec.getOutputBuffers();
            boolean isAudioEOS = false;
            long startMs = System.currentTimeMillis();
            while(!Thread.interrupted()){

                if (!isAudioEOS) {
                    isAudioEOS = putBufferToDecoder(audioCodec, audioExtractor,inputBuffers);

                }
                int outputBufferIndex = audioCodec.dequeueOutputBuffer(audioBufferInfo,10000);

                switch (outputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.v(TAG, "format changed");
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.v(TAG, "超时");
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        outputBuffers = audioCodec.getOutputBuffers();
                        Log.v(TAG, "output buffers changed");
                        break;
                    default:
                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        //延时操作
                        //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                        sleepRender(audioBufferInfo, startMs);
                        if (audioBufferInfo.size > 0) {
                            if (mAudioOutTempBuf.length < audioBufferInfo.size) {
                                mAudioOutTempBuf = new byte[audioBufferInfo.size];
                            }
                            outputBuffer.position(0);
                            outputBuffer.get(mAudioOutTempBuf, 0, audioBufferInfo.size); //byteBuffer 转 byte[]
                            outputBuffer.clear();
                            if (audioTrack != null)
                                audioTrack.write(mAudioOutTempBuf, 0, audioBufferInfo.size);
                        }
                        //mCurrentPosition = audioBufferInfo.presentationTimeUs / 1000;
                        //
                        audioCodec.releaseOutputBuffer(outputBufferIndex, false);
                        break;
                }

                if ((audioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.v(TAG, "buffer stream end");
                    break;
                }
            }
            audioCodec.stop();
            audioCodec.release();
            audioExtractor.release();
            audioTrack.stop();
            audioTrack.release();

        }
    }

//    private class AudioThread extends Thread {
//        private int audioInputBufferSize;
//
//        private AudioTrack audioTrack;
//
//        @Override
//        public void run() {
//            MediaExtractor audioExtractor = new MediaExtractor();
//            MediaCodec audioCodec = null;
//            try {
//                audioExtractor.setDataSource("/sdcard/Movies/1.mp4");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
//                MediaFormat mediaFormat = audioExtractor.getTrackFormat(i);
//                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
//                if (mime.startsWith("audio/")) {
//                    audioExtractor.selectTrack(i);
//                    int audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
//                    int audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//
//
//
//                    //<这里主要是关于输入缓冲大小的计算>
//                    int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
//                            (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
//                            AudioFormat.ENCODING_PCM_16BIT);
//                    int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
//                    audioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
//                    int frameSizeInBytes = audioChannels * 2;
//                    audioInputBufferSize = (audioInputBufferSize / frameSizeInBytes) * frameSizeInBytes;
//                    //</这里主要是关于输入缓冲大小的计算>
//
//
//                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                            audioSampleRate,
//                            (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
//                            AudioFormat.ENCODING_PCM_16BIT,
//                            audioInputBufferSize,
//                            AudioTrack.MODE_STREAM);
//                    audioTrack.play();
//
//                    Log.v(TAG, "audio play");
//                    //
//                    try {
//                        audioCodec = MediaCodec.createDecoderByType(mime);
//                        audioCodec.configure(mediaFormat, null, null, 0);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//            }
//            if (audioCodec == null) {
//                Log.v(TAG, "audio decoder null");
//                return;
//            }
//            audioCodec.start();
//            //
//
//            final ByteBuffer[] buffers = audioCodec.getOutputBuffers();
//            int sz = buffers[0].capacity();
//            if (sz <= 0)
//                sz = audioInputBufferSize;
//            byte[] mAudioOutTempBuf = new byte[sz];
//
//            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
//            ByteBuffer[] inputBuffers = audioCodec.getInputBuffers();
//            ByteBuffer[] outputBuffers = audioCodec.getOutputBuffers();
//            boolean isAudioEOS = false;
//            long startMs = System.currentTimeMillis();
//
//            while (!Thread.interrupted()) {
////                if (!isPlaying) {
////                    continue;
////                }
//                if (!isAudioEOS) {
//                    isAudioEOS = putBufferToDecoder( audioCodec, audioExtractor,inputBuffers);
//                }
//                //
//                int outputBufferIndex = audioCodec.dequeueOutputBuffer(audioBufferInfo,10000);
//                switch (outputBufferIndex) {
//                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
//                        Log.v(TAG, "format changed");
//                        break;
//                    case MediaCodec.INFO_TRY_AGAIN_LATER:
//                        Log.v(TAG, "超时");
//                        break;
//                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
//                        outputBuffers = audioCodec.getOutputBuffers();
//                        Log.v(TAG, "output buffers changed");
//                        break;
//                    default:
//                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//                        //延时操作
//                        //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//                        sleepRender(audioBufferInfo, startMs);
//                        if (audioBufferInfo.size > 0) {
//                            if (mAudioOutTempBuf.length < audioBufferInfo.size) {
//                                mAudioOutTempBuf = new byte[audioBufferInfo.size];
//                            }
//                            outputBuffer.position(0);
//                            outputBuffer.get(mAudioOutTempBuf, 0, audioBufferInfo.size); //byteBuffer 转 byte[]
//                            outputBuffer.clear();
//                            if (audioTrack != null)
//                                audioTrack.write(mAudioOutTempBuf, 0, audioBufferInfo.size);
//                        }
//                        //mCurrentPosition = audioBufferInfo.presentationTimeUs / 1000;
//                        //
//                        audioCodec.releaseOutputBuffer(outputBufferIndex, false);
//                        break;
//                }
//
//                if ((audioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    Log.v(TAG, "buffer stream end");
//                    break;
//                }
//            }//end while
//            audioCodec.stop();
//            audioCodec.release();
//            audioExtractor.release();
//            audioTrack.stop();
//            audioTrack.release();
//        }
//
//    }


//    private int getMediaTrackIndex(MediaExtractor videoExtractor , String MEDIA_TYPE){
//        int trackIndex = -1;
//        for (int i=0 ; i<videoExtractor.getTrackCount() ; i++){
//            MediaFormat mediaFormat=videoExtractor.getTrackFormat(i);
//            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
//            if(mime.startsWith(MEDIA_TYPE)){
//                trackIndex=i;
//                break;
//            }
//        }
//
//
//        return trackIndex;
//    }

    private boolean putBufferToDecoder(MediaCodec decoder,MediaExtractor extractor,ByteBuffer[] inputBuffers){
        boolean isEOF=false;
        int bufferIndex=decoder.dequeueInputBuffer(1000);
        Log.i(TAG, "dequeue buffer index is " + bufferIndex);

        if(bufferIndex>0){
            ByteBuffer inputBuffer = inputBuffers[bufferIndex];
            int sampleSize = extractor.readSampleData(inputBuffer,0);

            if(sampleSize < 0){
                decoder.queueInputBuffer(bufferIndex,0,0,0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEOF = true;
            }else{
                decoder.queueInputBuffer(bufferIndex,0, sampleSize, extractor.getSampleTime(),0);
                extractor.advance();
            }

        }
        return isEOF;
    }

    private int getMediaTrackIndex(MediaExtractor mediaExtractor,String MEDIA_TYPE){
        int trackIndex = -1;
        for(int i=0 ; i < mediaExtractor.getTrackCount() ; i++){
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            if(mediaFormat.getString(MediaFormat.KEY_MIME).startsWith(MEDIA_TYPE)){
                trackIndex = i;
                break;
            }
        }
        return trackIndex;
    }

    //延迟渲染
    private void sleepRender(MediaCodec.BufferInfo audioBufferInfo, long startMs) {
        while (audioBufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
