package cn.ac.iscas.xlab.droidfacedog.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lisongting on 2017/6/6.
 */

//用来管理音频的播放
public class AudioManager {

    public static final String TAG = "AudioManager";
    //用来表示没有播放音频
    public static final int CURRENT_NOT_PLAYING = -1;
    private int currentId;
    private boolean isPlaying;

    private Context context;

    //使用Map来维护整个要播放的音频列表
    private Map<Integer,MediaPlayer> audioMap;

    private MediaPlayer currentPlayer;

    String[] ttsFileList = {
            "tts/part0.mp3",
            "tts/part1.mp3",
            "tts/part2.mp3",
            "tts/part3.mp3",
            "tts/part4.mp3",
            "tts/part5.mp3",
            "tts/part6.mp3",
            "tts/part7.mp3",
            "tts/part8.mp3",
            "tts/part9.mp3",
            "tts/part10.mp3",
            "tts/part11.mp3",
            "tts/part12.mp3",
            "tts/part13.mp3",

    };

    public interface AudioCompletionCallback {
        //表示已经播放完的音频id
        void onComplete(int id);
    }


    public AudioManager(Context context) {
        this.context = context;
        audioMap = new HashMap<>();
        currentId = CURRENT_NOT_PLAYING;
        isPlaying = false;
    }

    public void loadTts() {
        new Thread() {
            public void run(){
                for(int i = 0; i < ttsFileList.length; i++) {
                    try {
                        AssetFileDescriptor afd = context.getAssets().openFd(ttsFileList[i]);
                        MediaPlayer mp = new MediaPlayer();
                        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        mp.prepare();

                        Log.i(TAG, "Loading ttsList[" + Integer.toString(i) + "]");
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                isPlaying = false;
                                currentId = CURRENT_NOT_PLAYING;
                            }
                        });
                        audioMap.put(i,mp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }


    public void play(int audioId) {
        Log.i(TAG, "AudioManager在播放：" + audioId + "号音频");
        currentPlayer = audioMap.get(audioId);
        currentPlayer.start();
        isPlaying = true;
        currentId = audioId;
    }

    public void playAsync(int audioId, final AudioCompletionCallback completionCallback) {
        Log.i(TAG, "AudioManager在播放：" + audioId + "号音频");
        currentPlayer = audioMap.get(audioId);
        currentPlayer.start();
        isPlaying = true;
        currentId = audioId;

        currentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                completionCallback.onComplete(currentId);
            }
        });
    }

    public void releaseMemory() {
        for(int i=0;i<audioMap.size();i++) {
            MediaPlayer mp = audioMap.remove(i);
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.release();
        }
        audioMap = null;
        isPlaying = false;
        currentId = CURRENT_NOT_PLAYING;
    }

    public void pause() {
        if (!isPlaying) {
            return;
        }
        if (currentPlayer.isPlaying()) {
            currentPlayer.pause();
        }
        isPlaying = false;
    }

    //继续播放
    public void resume() {
        if (isPlaying) {
            return;
        }
        currentPlayer.start();
        isPlaying = true;
    }

    public int getCurrentId() {
        return currentId;
    }

    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
