package com.brian19109.weatherapi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private boolean flag = false;
    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = findViewById(R.id.frameLayout);
        Initial();
        if (flag) {
            //權限取得成功，載入MapFragment
            MapFragment mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, mapFragment).commit();
        }
    }

    //確認是否已經取得定位權限，
    private void Initial() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission[0]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission[0]}, 101);
        } else {
            flag = true;
        }
    }

    //每次的ActivityCompat.requestPermission會觸發此Method監聽return的結果，再依結果執行相對應事件
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //權限取得成功，載入MapFragment
                flag = true;
                MapFragment mapFragment = new MapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, mapFragment).commit();
            } else {
                ////權限取得失敗，告知User
                new AlertDialog.Builder(this)
                        .setTitle("權限請求失敗")
                        .setMessage("因無法取得相關定位權限，請稍後再式")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                        .show();
            }
        }
    }
}