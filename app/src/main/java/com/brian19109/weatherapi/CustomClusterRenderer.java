package com.brian19109.weatherapi;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomClusterRenderer extends DefaultClusterRenderer<MyItem> {

    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
    }

    //把marker的相關內容都設定好
    @Override
    protected void onBeforeClusterItemRendered(@NonNull MyItem item, @NonNull MarkerOptions markerOptions) {
        markerOptions.icon(item.getBitmap());
        markerOptions.title(item.getTitle());
        markerOptions.snippet(item.getSnippet());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
