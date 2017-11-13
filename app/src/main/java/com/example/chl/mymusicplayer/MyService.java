package com.example.chl.mymusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;

import java.io.IOException;
import java.util.List;

import com.example.chl.mymusicplayer.aidl.Callback;
import com.example.chl.mymusicplayer.aidl.MyServiceCall;

public class MyService extends Service {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    public List<String> myList ;
    private int myPosition;
    private Boolean isPause = false;
    private Callback mCallback;

    private MyServiceCall myServiceCall = new MyServiceCall.Stub() {

        @Override
        public void setData(List<String> list) throws RemoteException {
            myList = list;
        }

        @Override
        public int getData(byte[] bytes) {
            //将播放器的状态写回
            if (mediaPlayer.isPlaying()) {
                bytes[0] = 0;
                return myPosition;
            } else if (isPause) {
                bytes[0] = 1;
                return myPosition;
            }
            return -1;
        }

        @Override
        public void last() throws RemoteException {
            if (myPosition >= 1) {
                myPosition--;
            }
            play(myPosition);
        }

        @Override
        public void next() throws RemoteException {
            if (myPosition <= myList.size() - 2) {
                myPosition++;
            } else {
                myPosition = 0;
            }
            play(myPosition);
        }

        @Override
        public void pause() throws RemoteException {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPause = true;
            } else {
                mediaPlayer.start();
                isPause = false;
            }
        }

        @Override
        public void play(int position) throws RemoteException {
            myPosition = position;
            mCallback.callback(myPosition); //将当前播放的position通过回调返回activity
            try {
                //播放前重置
                mediaPlayer.reset();
                //设置数据源
                mediaPlayer.setDataSource(myList.get(myPosition));
                //异步准备资源
                mediaPlayer.prepareAsync();
                //由于是异步准备，所以要设置该监听，等准备完毕就start
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setCallback(Callback callback) throws RemoteException {
            mCallback = callback;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //监听一首歌播放完毕后，播放下一首
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    myServiceCall.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myServiceCall.asBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}
