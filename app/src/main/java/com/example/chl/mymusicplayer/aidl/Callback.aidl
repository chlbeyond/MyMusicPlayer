package com.example.chl.mymusicplayer.aidl;

interface Callback {
    //这里不要加oneway,否则程序不会等这个方法执行完
    void callback(int position);
}
