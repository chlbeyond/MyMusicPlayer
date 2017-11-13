package com.example.chl.mymusicplayer.aidl;

import com.example.chl.mymusicplayer.aidl.Callback;

interface MyServiceCall {

    //不是基本参数一定要表明是in还是out,这里就一定要写in
    void setData(in List<String> list);

    //由于基本参数只能是in，所以这里使用byte[]
    int getData(out byte[] bytes);

    void last();

    void next();

    void pause();

    void play(int position);

    oneway void setCallback(Callback callback);
}
