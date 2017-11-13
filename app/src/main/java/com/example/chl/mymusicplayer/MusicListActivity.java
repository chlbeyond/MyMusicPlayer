package com.example.chl.mymusicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chl.mymusicplayer.aidl.Callback;
import com.example.chl.mymusicplayer.aidl.MyServiceCall;

import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {

    private ListView listView;
    private MyServiceCall serviceCall;
    private List<Song> list;
    private List<String> myList;
    private TextView minisinger;
    private TextView minisong;
    private TextView play;
    private boolean isPlaying = false;

    private Handler handler = new Handler();

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            serviceCall = MyServiceCall.Stub.asInterface(service);
            try {
                serviceCall.setData(myList);

                //如果activity从后台回到前台，而此时音乐处于暂停或播放态，如果没有下面的代码
                //那么mini条的UI还是处于初态，所以此时要根据音乐播放状态来更新UI
                byte[] bytes = new byte[1];
                int position = serviceCall.getData(bytes);
                if (position != -1) {
                    if (bytes[0] == 0) {
                        setText(position);
                    } else if (bytes[0] == 1) {
                        isPlaying = false;
                        play.setText("播放");
                        minisinger.setText(list.get(position).singer);
                        minisong.setText(list.get(position).song);
                    }
                }

                //设置回调更新UI
                serviceCall.setCallback(new Callback.Stub() {
                    @Override
                    public void callback(final int position) throws RemoteException {
                        //如果是服务自动播放下一首歌，那么callback就不是运行在UI线程中
                        //所以无论哪种情况都将它post回UI线程执行UI,就保证不会错了
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                setText(position);
                                if (position == 0) {
                                    Toast.makeText(MusicListActivity.this, "已经是第一首歌", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceCall = null;
        }
    };
    private void bindServiceConnection() {
        Intent intent = new Intent(MusicListActivity.this, MyService.class);
        startService(intent);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        getMusicData();
        bindServiceConnection();
        minisinger = (TextView) findViewById(R.id.minisinger);
        minisong = (TextView) findViewById(R.id.minisong);
        play = (TextView) findViewById(R.id.play);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new MyAdapter(list));
        setListener();

        View footView = LayoutInflater.from(MusicListActivity.this).inflate(R.layout.listfoot, listView, false);
        listView.addFooterView(footView);
    }

    private void setListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (serviceCall != null) {
                    try {
                        serviceCall.play(position);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void miniClick(View v) {
        switch (v.getId()) {
            case R.id.last:
                try {
                    serviceCall.last();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play:
                if (play.getText().equals("未点歌")) {
                    Toast.makeText(MusicListActivity.this, "请点击要播放的歌曲", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (isPlaying) {
                    isPlaying = false;
                    play.setText("播放");
                }  else {
                    isPlaying = true;
                    play.setText("暂停");
                }
                try {
                    serviceCall.pause();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.next:
                try {
                    serviceCall.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.mini:
                //消费点击事件，防止点击事件传递到listView
                break;
        }
    }

    private void setText(int position) {
        isPlaying = true;
        play.setText("暂停");
        minisinger.setText(list.get(position).singer);
        minisong.setText(list.get(position).song);
    }

    //获得本地音乐作为数据源
    private void getMusicData() {
        list = new ArrayList<Song>();
        myList = new ArrayList<>();
        String path;
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                song.singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //一般歌名的格式为“歌手-歌名”，如果不想歌名带着歌手名，可以打开下面的注释
//                if (song.song.contains("-")) {
//                    String[] str = song.song.split("-");
//                    song.singer = str[0]; //歌手
//                    song.song = str[1];   //歌名
//                }
                list.add(song);
                myList.add(path);
            }
            cursor.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(sc);
    }
}
