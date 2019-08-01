package com.example.mediaplayertest;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaEditor {
    private static final String mSdcard = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String srcPath = mSdcard + "/Movies/1.mp4";
    private static final String dstPath_cut = mSdcard + "/Movies/result_cut.mp4";
    private static final String dstPath_changeSpeed = mSdcard + "/Movies/result_changeSpeed.mp4";
    private static final String TAG = "MediaEditor";

    private MediaExtractor mediaExtractor;
    private MediaCodec  mediaCodec;
    private MediaMuxer mediaMuxer;
    MediaFormat videoFormat,audioFormat;
    int videoTrackIndex = -1;
    int videoMaxInputSize = -1;
    int sourceVideoTrack = 0;
    int audioTrackIndex = -1;
    int audioMaxInputSize = -1;
    int sourceAudioTrack = 0;

    public boolean decodeVAFormatToMuxer(){



        mediaExtractor = new MediaExtractor();
        File file = new File(dstPath_cut);
        if(file.exists()){
            Log.i(TAG, "decodeVAFormatToMuxer: delete existing file");
            file.delete();
        }
        try {
            mediaExtractor.setDataSource(srcPath);
            mediaMuxer = new MediaMuxer(dstPath_cut, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }



        for(int i=0;i<mediaExtractor.getTrackCount();i++){
            if(mediaExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME).startsWith("video/")){
                sourceVideoTrack = i;

            }else if(mediaExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME).startsWith("audio/")){
                sourceAudioTrack = i;

            }
        }

        videoFormat = mediaExtractor.getTrackFormat(sourceVideoTrack);
        audioFormat = mediaExtractor.getTrackFormat(sourceAudioTrack);

        //视频
        int vWidth = videoFormat.getInteger(MediaFormat.KEY_WIDTH);
        int vHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT);
//        long vDuration = videoFormat.getInteger(MediaFormat.KEY_DURATION); // us
        int rotation = videoFormat.getInteger(MediaFormat.KEY_ROTATION);
        videoMaxInputSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        Log.i(TAG+"rotation", " decodeVideo: "+ " Vwidth:"+vWidth+" Vheight:"+vHeight+ " Vrotation:"+rotation);
        videoTrackIndex = mediaMuxer.addTrack(videoFormat);
        mediaMuxer.setOrientationHint(rotation);
        //音频
        int sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        audioMaxInputSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        //audioDuration = audioFormat.getLong(MediaFormat.KEY_DURATION);
        Log.d(TAG, "sampleRate is " + sampleRate
                + ";channelCount is " + channelCount
                + ";audioMaxInputSize is " + audioMaxInputSize
                + ";audioDuration is "
        );

        audioTrackIndex = mediaMuxer.addTrack(audioFormat);



        return true;
    }


    /**
    *@param beginTime       剪切视频开始的时间
     @param endTime         剪切视频结束的时间
     @param isChangeSpeed   float  ：   1:不变速  ; 2：放慢两倍 ； 0.5加快两倍

     */



    public boolean process_cut(int beginTime, int endTime,double isChangeSpeed){
        ByteBuffer inputBuffer = ByteBuffer.allocate(videoMaxInputSize);

        mediaMuxer.start();

        //视频
        mediaExtractor.selectTrack(sourceVideoTrack);
        MediaCodec.BufferInfo videoInfo = new MediaCodec.BufferInfo();
        mediaExtractor.seekTo(beginTime * 1000000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long v_offset = videoInfo.presentationTimeUs = mediaExtractor.getSampleTime();



        while(true){
            int sampleSize = mediaExtractor.readSampleData(inputBuffer,0);
            if(sampleSize<0){
                mediaExtractor.unselectTrack(sourceVideoTrack);
                break;
            }
            int trackIndex = mediaExtractor.getSampleTrackIndex();
            //获取时间戳
            long presentationTimeUs = mediaExtractor.getSampleTime();
            //获取帧类型，只能识别是否为I帧
            int sampleFlag = mediaExtractor.getSampleFlags();
            Log.d(TAG+"video", "trackIndex is " + trackIndex
                    + ";presentationTimeUs is " + presentationTimeUs
                    + ";sampleFlag is " + sampleFlag
                    + ";sampleSize is " + sampleSize);
            if(beginTime == endTime || presentationTimeUs > endTime*1000000){
                mediaExtractor.unselectTrack(sourceVideoTrack);
                break;
            }

            videoInfo.offset = 0;
            videoInfo.size = sampleSize;
            videoInfo.flags = sampleFlag;
            videoInfo.presentationTimeUs=Math.round((presentationTimeUs-v_offset) * isChangeSpeed);
            mediaMuxer.writeSampleData(videoTrackIndex, inputBuffer, videoInfo);
            mediaExtractor.advance();


        }

        //音频
        mediaExtractor.selectTrack(sourceAudioTrack);
        MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();
        mediaExtractor.seekTo(beginTime * 1000000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        long a_offset = audioInfo.presentationTimeUs = mediaExtractor.getSampleTime();

        while(true){
            int sampleSize = mediaExtractor.readSampleData(inputBuffer,0);
            if(sampleSize<0){
                mediaExtractor.unselectTrack(sourceAudioTrack);
                break;
            }
            int trackIndex = mediaExtractor.getSampleTrackIndex();
            //获取时间戳
            long presentationTimeUs = mediaExtractor.getSampleTime();
            //获取帧类型，只能识别是否为I帧
            int sampleFlag = mediaExtractor.getSampleFlags();
            Log.d(TAG+"audio", "trackIndex is " + trackIndex
                    + ";presentationTimeUs is " + presentationTimeUs
                    + ";sampleFlag is " + sampleFlag
                    + ";sampleSize is " + sampleSize);
            if(beginTime == endTime || presentationTimeUs > endTime*1000000){
                mediaExtractor.unselectTrack(sourceAudioTrack);
                break;
            }

            audioInfo.offset = 0;
            audioInfo.size = sampleSize;
            audioInfo.flags = sampleFlag;
            audioInfo.presentationTimeUs=presentationTimeUs-a_offset;
            mediaMuxer.writeSampleData(audioTrackIndex, inputBuffer, audioInfo);
            mediaExtractor.advance();


        }
        mediaMuxer.stop();
        mediaMuxer.release();
        mediaExtractor.release();
        mediaExtractor = null;
        
        return true;
    }


}
