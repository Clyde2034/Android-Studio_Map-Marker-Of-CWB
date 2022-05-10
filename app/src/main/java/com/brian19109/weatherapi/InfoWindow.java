package com.brian19109.weatherapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindow implements GoogleMap.InfoWindowAdapter{

    private final View mWindow;
    private Context mContext;

    public InfoWindow(Context context) {
        mContext=context;
        //帶入另外寫給InfoWindow的Layout
        mWindow= LayoutInflater.from(context).inflate(R.layout.info_window,null);
    }
    private void rendowWindowText(Marker marker,View view){
        String title=marker.getTitle();
        TextView tv_title=view.findViewById(R.id.tv_title);
        if(!title.isEmpty()){
            tv_title.setText(title);
        }

        String snippet=marker.getSnippet();
        TextView tv_content=view.findViewById(R.id.tv_content);
        if(!snippet.isEmpty()){
            tv_content.setText(snippet);
        }
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        rendowWindowText(marker,mWindow);
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        rendowWindowText(marker,mWindow);
        return mWindow;
    }
}
