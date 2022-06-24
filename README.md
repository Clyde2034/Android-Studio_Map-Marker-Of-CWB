# Android Studio-Map Marker of CWB
This project is fetch data from Taiwan CWB with API，after that using JSON parser the data and create marker on the map.

>API Require
>
>>okhttp  
>>Maps SDK for Android  
>>Distance Matrix API  
>>CWB API Document:https://opendata.cwb.gov.tw/dist/opendata-swagger.html  

>UI and Java Code  
>>UI：activity_main.xml、fragment_map.xml、info_window.xml  
>>Java：MainActivity.java、MapFragment.java、MyItem.java、InfoWindow.java、DistanceSplit.java、CustomClusterRenderer.java  

<p align="center">
  <img align="left" src="https://user-images.githubusercontent.com/41913354/167805507-55a9025c-adbc-45e1-bf30-c7fc2adea917.png" width="250"/>
  <img align="center" src="https://user-images.githubusercontent.com/41913354/167808333-9cdd3e4e-64f1-4d1d-a40e-17b9b2138794.gif" width="250"/>
  <img align="right" src="https://user-images.githubusercontent.com/41913354/167807003-a309820a-b9c7-4d42-a58a-457f75930180.gif" width="250"/>
</p>

## AndroidManifest.xml and other Environment
```
    //...
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    //...
-------------------------------------------------------------------------------------------
dependencies {
    //...
    implementation 'com.google.android.gms:play-services-maps:18.0.2' //mapView
    implementation 'com.google.android.gms:play-services-location:19.0.1' //FusedLocationProviderClient

    implementation 'com.squareup.okhttp3:okhttp:4.7.2'//okhttp
    implementation 'com.squareup.okhttp3:okhttp-urlconnection:4.7.2'//log connenting status
    implementation 'com.squareup.okhttp3:logging-interceptor:4.7.2'//log connenting status

    implementation 'com.google.maps.android:android-maps-utils:2.2.3'//Maps SDK for Android Utility Library
    //...
}  
-------------------------------------------------------------------------------------------
build.gradle(Project:(Project Name)) {
    plugins {
    //..
    id 'com.google.android.libraries.mapsplatform.secrets-gradle-plugin' version '2.0.1' apply false
    //..
    }
}
//Used to Support Maps SDK for Android on the API Level 28 and higher.
-------------------------------------------------------------------------------------------
build.gradle(Module:(Project Name.app)) {
    plugins {
    //..
    id 'com.google.android.libraries.mapsplatform.secrets-gradle-plugin'
    //..
    }
}
//Used to Support Maps SDK for Android on the API Level 28 and higher.
```

## The project is complex than normal small project, so decide to using text to dexcription what the project is doing and every file purpose.
# CWB_KEY and Map API_KEY must be replace.
>UI
>>activity_main.xml:This activity is first create when start the application, and check the permission.  
>>fragment_map.xml:Embeded the map to the fragment and some ui element.  
>>info_window.xml:When marker exists in the maps, the window will show the marker content on the top of marker.  

>Java  
>>MainActivity.java:Permission request and transaction to maps.  
>>MapFragment.java:Kernel Tech, like api data parse、two point of distance calculate by Distance Matrix from Google...  
>>MyItem.java:An marker object, used to marker cluster.
>>InfoWindow.java:Setting the InfoWindow layout and implement the InfoWindowAapter.    
>>DistanceSplit.java:Reprocess the distance data unit, like day、minute.  
>>CustomClusterRenderer.java:The Render setting the marker icon、title、snippet to ClusterManager

