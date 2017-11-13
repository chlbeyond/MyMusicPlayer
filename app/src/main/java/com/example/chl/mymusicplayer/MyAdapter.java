package com.example.chl.mymusicplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Chen Hailiang on 2017/6/10.
 */

public class MyAdapter extends BaseAdapter {

    private List<Song> list;

    public MyAdapter(List<Song> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item, parent, false);
            holder = new ViewHolder();
            holder.song = (TextView) convertView.findViewById(R.id.song);
            holder.singer = (TextView) convertView.findViewById(R.id.singer);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Song song = list.get(position);

        holder.song.setText(song.song.toString());
        holder.singer.setText(song.singer.toString());
        return convertView;
    }

    private class ViewHolder {
        public TextView song;
        public TextView singer;
    }

}
