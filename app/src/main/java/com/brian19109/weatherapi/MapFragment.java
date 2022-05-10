package com.brian19109.weatherapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private Button btn_bicycle, btn_scooter, btn_car, btn_clear;
    private TextView tv_currentmode;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private SupportMapFragment supportMapFragment;
    private View mapView;
    private ArrayList<HashMap<String, String>> receiveDATA = new ArrayList<>();
    private final LatLng[] myLocation = new LatLng[1];
    private GoogleMap map;
    private Handler mainHandler = new Handler();
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build();
    String CWB_Auth = "CWB-DC98BC30-EA33-4843-8569-8DDC20B6E646";
    private String Arrival_Time_rangeMax = "";
    private String Arrival_Time = "";
    private String Current_Time="";
    private ClusterManager<MyItem> clusterManager;
    private ImageButton imagebtn_expandable;
    private LinearLayout linearLayout_expandContent;
    private CardView cardview;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //對已知的UI Component binding id
        findViewByID(view);

        //初始化Map和相關按鈕事件，告知要放入哪個容器，並且一定要時做MapAsync，因Maps是網路資料，要使用異步處理
        //onMapReady是個Interface，需要implement進入實做
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        supportMapFragment.getMapAsync(this);
        mapView = supportMapFragment.getView();
        btn_bicycle.setOnClickListener(BICYCLE);
        btn_scooter.setOnClickListener(SCOOTER);
        btn_car.setOnClickListener(CAR);
        btn_clear.setOnClickListener(CLEAR);

        imagebtn_expandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(linearLayout_expandContent.getVisibility()==View.VISIBLE){
                    TransitionManager.beginDelayedTransition(cardview,new AutoTransition());
                    linearLayout_expandContent.setVisibility(View.GONE);
                    imagebtn_expandable.setImageResource(R.drawable.ic_baseline_expand_more_24);
                }else{
                    TransitionManager.beginDelayedTransition(cardview,new AutoTransition());
                    linearLayout_expandContent.setVisibility(View.VISIBLE);
                    imagebtn_expandable.setImageResource(R.drawable.ic_baseline_expand_less_24);
                }
            }
        });

        return view;
    }

    //初始打開Map會執行底下的onMapReady
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap mMap) {
        //設定Map UI，允許定位按鈕點擊且設定Map地形為Normal
        map = mMap;
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //此處為設定定位按鈕的位置，原本預設在右上角，移動至右下角，本身google無提供變更方法，只能使用底下方法移動
//        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        layoutParams.setMargins(0, 0, 30, 150);

        //fusedLocation用來取得當下位置，並把經緯度存放起來在myLocation以便後續使用
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLocation[0] = new LatLng(location.getLatitude(), location.getLongitude());

                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.975650, 120.973882), 7));
                        } else {
                            //Taipei 101 LatLng
                            //myLocation[0] = new LatLng(25.033964, 121.564468);
                        }
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(getActivity(), "Failed to get currnt last location.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //腳踏車按鈕點擊監聽事件
    //底下摩托車、汽車同樣內容，不再贅述
    private View.OnClickListener BICYCLE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //此段對按鈕控制，在撈取資料過程不得按其他按鈕，避免負荷量過載等之類問題發生
            btn_bicycle.setEnabled(false);
            btn_scooter.setEnabled(false);
            btn_car.setEnabled(false);
            btn_clear.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            //此段對按鈕控制，在撈取資料過程不得按其他按鈕，避免負荷量過載等之類問題發生

            //稍後對distance matrix的網址下參數送給google的server計算後回傳結果
            tv_currentmode.setText("Current Mode=Bicycling");

            //每次點擊按鈕都清理之前存放的結果，receiveDATA是存放各站經緯度、名稱、預計抵達時間、氣象等資料
            receiveDATA.clear();
            map.clear();
            try {
                clusterManager.clearItems();
            }catch (Exception e){

            }

            //使用子線程方式呼叫取得資料，因為資料量較大，使用子線程可避免佔用主線程時間太久而出現android自動skip frame的機制
            //否則如果以主線程就是一般正常撈取資料的方式，會出現介面卡住的情況，容易使人家認為APP當掉了，UX(使用者體驗)較差
            new GET_data("bicycling", "").start();

        }
    };

    //摩托車按鈕點擊監聽事件
    private View.OnClickListener SCOOTER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btn_bicycle.setEnabled(false);
            btn_scooter.setEnabled(false);
            btn_car.setEnabled(false);
            btn_clear.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            tv_currentmode.setText("Current Mode=scooter");
            receiveDATA.clear();
            map.clear();
            try {
                clusterManager.clearItems();
            }catch (Exception e){

            }
            //摩托車的參數為避開高速公路，因此第二個parameter丟入此內容
            //&avoid=highays 避開高速公路
            //&avoid=tolls 避開收費路段
            //詳情參考:https://developers.google.com/maps/documentation/distance-matrix/overview#avoid
            new GET_data("driving", "&avoid=highways").start();
        }
    };

    //汽車按鈕點擊監聽事件
    //內容同腳踏車
    private View.OnClickListener CAR = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btn_bicycle.setEnabled(false);
            btn_scooter.setEnabled(false);
            btn_car.setEnabled(false);
            btn_clear.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            tv_currentmode.setText("Current Mode=Driving");
            receiveDATA.clear();
            map.clear();
            try {
                clusterManager.clearItems();
            }catch (Exception e){

            }
            new GET_data("driving", "").start();
        }
    };

    //主要做內容清除的動作，把receiveDATA、Map的圖釘、marker cluster清空，Marker cluster後面提到，用途就是根據
    //Map的Zoom level自動把圖釘變成叢集
    private View.OnClickListener CLEAR = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btn_bicycle.setEnabled(false);
            btn_scooter.setEnabled(false);
            btn_car.setEnabled(false);
            btn_clear.setEnabled(false);
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    receiveDATA.clear();
                    map.clear();
                    try {
                        clusterManager.clearItems();
                    }catch (Exception e){

                    }
                    btn_bicycle.setEnabled(true);
                    btn_scooter.setEnabled(true);
                    btn_car.setEnabled(true);
                    btn_clear.setEnabled(true);
                    tv_currentmode.setText("");
                    Toast.makeText(getActivity(), "Marker清除成功", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    //重點部分
    private void GET_DATA_UnCheckOver(String traffice_mode, String avoid) {
        //:=%3A chractor code
        //CWB的OpenAPI使用/v1/rest/datastore/F-D0047-093這隻資料
        //而取樣的資料是有規律的，各縣市的兩天天氣預報尾數以4遞增
        //ex:F-D0047-001=宜蘭縣、F-D0047-005=桃園市......
        for (int country_data_code = 1; country_data_code <= 85; country_data_code += 4) {
            String URL;
            int country_data_code_temp = country_data_code;

            //此處單純寫好要request的url，但是數字的部分01會因為是integer而變成1，因此001~009的F-D0047-00就多了一個0
            //直接看URL可能比較看得懂，其餘部分都相同
            Predict_Arrival_Time(0, 0, 0);
            if (country_data_code >= 1 && country_data_code <= 9) {
                URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093?Authorization=" + CWB_Auth + "&format=JSON&locationId=F-D0047-00" + country_data_code + "&elementName=Wx,PoP6h" + "&timeTo=" + Arrival_Time_rangeMax;
            } else {
                URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093?Authorization=" + CWB_Auth + "&format=JSON&locationId=F-D0047-0" + country_data_code + "&elementName=Wx,PoP6h" + "&timeTo=" + Arrival_Time_rangeMax;
            }

            //對此URL做request的動作
            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("User-Agent:", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36")
                    .build();

            try {
                //此response為上方request之結果，接著進行JSON的解析，此處建議使用json parser相關工具對照著看會比較看得懂解析的步驟
                Response response = client.newCall(request).execute();
                JSONObject jsonObject = new JSONObject(response.body().string());
                JSONArray jsonArray = jsonObject.getJSONObject("records").getJSONArray("locations").getJSONObject(0).getJSONArray("location");
                //此處取得縣市前三個地區的觀測站資料，但有些地區不到三個只有兩個，例如嘉義縣還是嘉義市吧
                //location_Name=站點名稱
                //location_Lat,location_Lon=站點經緯度
                for (int cuntry_count = 0; cuntry_count < 3; cuntry_count++) {
                    String locations_Name = jsonObject.getJSONObject("records").getJSONArray("locations").getJSONObject(0).getString("locationsName");
                    String location_Name = "";
                    try {
                        location_Name = jsonArray.getJSONObject(cuntry_count).getString("locationName");
                    } catch (Exception e) {
                        continue;
                    }
                    String location_Lat = jsonArray.getJSONObject(cuntry_count).getString("lat");
                    String location_Lon = jsonArray.getJSONObject(cuntry_count).getString("lon");
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("location_Name", locations_Name + location_Name);
                    hashMap.put("location_Lat", location_Lat);
                    hashMap.put("location_Lon", location_Lon);
                    //此處先暫時取得各站的資訊，還未取得各站的天氣資訊，因為要根據抵達時間取得天氣資訊，所以先計算預計車程時間
                    GET_Distance(traffice_mode, avoid, location_Name, location_Lat, location_Lon, country_data_code_temp, hashMap);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //重點部分
    private void GET_Distance(String traffice_mode, String avoid, String location_Name, String location_Lat, String location_Lon, int index, HashMap hashMap) {

        String origin = "";
        String destination = "";
        try {
            //先根據站點的經緯度和目前位置來得知車程時間
            origin = URLEncoder.encode(String.valueOf(myLocation[0].latitude) + "," + String.valueOf(myLocation[0].longitude), "utf-8");
            destination = URLEncoder.encode(location_Lat + "," + location_Lon, "utf-8");
            String URL = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations=" + destination + "&mode=" + traffice_mode + "&language=zh-TW" + avoid + "&key=" + getString(R.string.api_key);
            Request request = new Request.Builder()
                    .url(URL)
                    .method("GET", null)
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());
            //此duration就是得知目前位置和傳送過來的經緯度的車程時間
            String duration = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getString("text");

            //而這邊一樣把duration做解析的動作，因為distance matrix return的JSON內容當中，時間格式並非是時間格式，單純以文字說明而已，ex:1 天 3 小時
            //而duration只會出現以下幾種文字說明
            //1.xx天xx小時 2.xx天 3.xx小時xx分鐘 4.xx分鐘
            //因此另外寫一個class去解析他以取得天、小時、分鐘的數字是多少
            DistanceSplit duration_object = new DistanceSplit(duration);
            String duration_day = duration_object.getDay();
            String duration_hour = duration_object.getHour();
            String duration_minute = duration_object.getMinute();
            //都得知到目的地要花幾天幾小時幾分鐘後，呼叫Predict_Arrival_Time此functon來計算實際抵達的時間為何時
            Predict_Arrival_Time(Integer.valueOf(duration_day), Integer.valueOf(duration_hour), Integer.valueOf(duration_minute));

            //時間區間取得之後就再重新從CWB open data取得資料一次，不過這次會帶入需要的時間為何時
            String Second_request_cwb;
            if (index >= 1 && index <= 9) {
                Second_request_cwb = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093?Authorization=" + CWB_Auth + "&format=JSON&locationId=F-D0047-00" + index + "&locationName=" + URLEncoder.encode(location_Name, "utf-8") + "&elementName=PoP6h,Wx&timeTo=" + Arrival_Time_rangeMax;
            } else {
                Second_request_cwb = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093?Authorization=" + CWB_Auth + "&format=JSON&locationId=F-D0047-0" + index + "&locationName=" + URLEncoder.encode(location_Name, "utf-8") + "&elementName=PoP6h,Wx&timeTo=" + Arrival_Time_rangeMax;
            }

            Request request_cwb = new Request.Builder()
                    .url(Second_request_cwb)
                    .method("GET", null)
                    .build();
            Response response_cwb = client.newCall(request_cwb).execute();
            //底下仍為JSON解析步驟，說明不易，建議直接在 https://opendata.cwb.gov.tw/dist/opendata-swagger.html#/ 上直接request對著結果比對就可以看出解析的邏輯
            //weatherElement_content_object_Wx=天氣情況的值
            //weatherElement_content_object_Wx_icon_value=天氣情況icon的值,不同值有對應的icon
            //weatherElement_content_object_PoP6h=降雨機率的值
            //weather_start_time_Wx=天氣情況的時間區間起,weather_end_time_Wx天氣情況的時間區間始,這兩個參數不一定要記錄,只是為了方便測試抓到的資料是否正確
            //weather_start_time_PoP6h=降雨機率的時間區間起,weather_end_time_PoP6h=降雨機率的時間區間始,這兩個參數不一定要記錄,只是為了方便測試抓到的資料是否正確
            //Arrival_Time=預計抵達時間
            //Current_Time=目前時間,如果使用模擬器大麼取得的時間會是電腦時間因為android模擬器的時間不太準確每次重新開啟都會跑掉,
            //如果是使用實機取得的就是手機時間

            JSONObject jsonObject_cwb = new JSONObject(response_cwb.body().string());
            JSONArray jsonArray_cwb = jsonObject_cwb.getJSONObject("records").getJSONArray("locations").getJSONObject(0).getJSONArray("location");
            JSONArray Wx_Array_Length = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time");
            JSONArray PoP6h_Array_Length = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time");
            String weatherElement_content_object_Wx = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(Wx_Array_Length.length() - 1).getJSONArray("elementValue").getJSONObject(0).getString("value");//Wx
            String weatherElement_content_object_Wx_icon_value = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(Wx_Array_Length.length() - 1).getJSONArray("elementValue").getJSONObject(1).getString("value");//Wx icon value;
            String weatherElement_content_object_PoP6h = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(PoP6h_Array_Length.length() - 1).getJSONArray("elementValue").getJSONObject(0).getString("value");//PoP6h
            String weather_start_time_Wx = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(Wx_Array_Length.length() - 1).getString("startTime");
            String weather_end_time_Wx = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(0).getJSONArray("time").getJSONObject(Wx_Array_Length.length() - 1).getString("endTime");
            String weather_start_time_PoP6h = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(PoP6h_Array_Length.length() - 1).getString("startTime");
            String weather_end_time_PoP6h = jsonArray_cwb.getJSONObject(0).getJSONArray("weatherElement").getJSONObject(1).getJSONArray("time").getJSONObject(PoP6h_Array_Length.length() - 1).getString("endTime");
            hashMap.put("Wx", weatherElement_content_object_Wx);
            hashMap.put("Wx_icon_value", weatherElement_content_object_Wx_icon_value);
            hashMap.put("PoP6h", weatherElement_content_object_PoP6h);
            hashMap.put("duration_time", duration);
            hashMap.put("CWB_start_Time_Wx", weather_start_time_Wx);
            hashMap.put("CWB_end_Time_Wx", weather_end_time_Wx);
            hashMap.put("CWB_start_Time_PoP6h", weather_start_time_PoP6h);
            hashMap.put("CWB_end_Time_PoP6h", weather_end_time_PoP6h);
            hashMap.put("Arrival_Time", Arrival_Time);
            hashMap.put("Current_Time", Current_Time);
            receiveDATA.add(hashMap);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    //新增Maps的Markers
    private void SHOW_markpoint(ArrayList arrayList) {
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap map) {
                map.clear();
                //每次呼叫都把zoom level調整,方便俯視全台灣
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.975650, 120.973882), 7));

                for (int i = 0; i < receiveDATA.size(); i++) {
                    //根據Wx_icon_value取得對應的icon,並把icon放入bitmap此變數
                    String uri = "@drawable/ic_" + receiveDATA.get(i).get("Wx_icon_value");
                    int icon = getResources().getIdentifier(uri, null, getActivity().getPackageName());
                    BitmapDescriptor bitmap = generateBitmapDescriptorFromRes(getActivity(), icon);

                    //marker資訊視窗的內容
                    String snippet_content = "預計時間：" + receiveDATA.get(i).get("duration_time") + "\n" +
                            "天氣：" + receiveDATA.get(i).get("Wx") + "\n" +
                            "降雨機率：" + receiveDATA.get(i).get("PoP6h") + "%" + "\n" +
                            "目前時間："+receiveDATA.get(i).get("Current_Time")+"\n"+
                            "預計抵達：" + receiveDATA.get(i).get("Arrival_Time") + "\n" +
                            "CWB_start_Time_Wx：" + receiveDATA.get(i).get("CWB_start_Time_Wx") + "\n" +
                            "CWB_end_Time_Wx：" + receiveDATA.get(i).get("CWB_end_Time_Wx") + "\n" +
                            "CWB_start_Time_PoP6h：" + receiveDATA.get(i).get("CWB_start_Time_PoP6h") + "\n" +
                            "CWB_end_Time_PoP6h：" + receiveDATA.get(i).get("CWB_end_Time_PoP6h");

                    //此處使用google maps marker cluster,會根據經緯度自動歸類為同一個叢集
                    //而此時不管是marker的設定或點擊事件還是marker的InfoWindow的設定或點擊事件都會交由clusterManager控管
                    Double lat = Double.valueOf(receiveDATA.get(i).get("location_Lat"));
                    Double lon = Double.valueOf(receiveDATA.get(i).get("location_Lon"));
                    //(*)
                    String title = receiveDATA.get(i).get("location_Name");
                    MyItem offsetItem = new MyItem(lat, lon, title, snippet_content, bitmap);
                    clusterManager.addItem(offsetItem);
                }
            }
        });

        clusterManager = new ClusterManager<MyItem>(getActivity(), map);
        //此處帶入自行客製化的InfoWindow，需寫一個class並實做GoogleMap.InfoWindowAdapter介面來更改
        clusterManager.getMarkerCollection().setInfoWindowAdapter(new InfoWindow(getActivity()));
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        //渲染器,這裡是用來設定marker要填入的title,snippet,icon,同樣需要實做介面來修改,這邊google做的蠻麻煩的
        //由於前面(*)此處的item都已經有先放入經緯度,標題,icon了,所以我們只要對每個marker把這些資訊放進來就好
        clusterManager.setRenderer(new CustomClusterRenderer(getActivity(), map, clusterManager));
    }

    //icon的處理，把Drawable convert to Bitmap,因marker的icon要求bitmap
    public static BitmapDescriptor generateBitmapDescriptorFromRes(Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                70,
                70);
        Bitmap bitmap = Bitmap.createBitmap(
                70,
                70,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //子線程執行
    class GET_data extends Thread {
        String traffic_mode;
        String avoid;

        public GET_data(String traffic_mode, String avoid) {
            this.traffic_mode = traffic_mode;
            this.avoid = avoid;
        }

        @Override
        public void run() {
            //此處是需要做的事情,也就是子線程做的事情,要注意這邊做的事情當中
            //不能有對UI做變動的事情,因為UI的改變是主線程在做的事情
            GET_DATA_UnCheckOver(traffic_mode, avoid);

            //主線程
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    //這邊的事件就是主線程要做的事,待子線程的事情完成後就會馬上執行這邊的事
                    progressBar.setVisibility(View.GONE);
                    btn_bicycle.setEnabled(true);
                    btn_scooter.setEnabled(true);
                    btn_car.setEnabled(true);
                    btn_clear.setEnabled(true);
                    SHOW_markpoint(receiveDATA);
                }
            });
        }
    }

    private void Predict_Arrival_Time(int day, int hour, int minute) {
        try {
            //先制定好要表達的時間格式
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));

            String now_time = formatter.format(new Date());
            Date now_time_unformat = null;
            now_time_unformat = formatter.parse(now_time);
            //此為當前時間，以yyyy-MM-dd'T'HH:mm:ss表示
            Current_Time=formatter.format(now_time_unformat);
            //這邊再利用Calender Object來直接增加天、小時、分鐘，更為方便快速
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Taipei"));
            calendar.setTime(now_time_unformat);
            calendar.add(Calendar.DATE, day);
            calendar.add(Calendar.HOUR, hour);
            calendar.add(Calendar.MINUTE, minute);
            String arrival_time = formatter.format(calendar.getTime());
            //目的抵達時間取得
            Arrival_Time = arrival_time;
            //接下來為取得時間區間的部分，因為CWB在取得資料的部分假設抵達時間是14:33，
            //而以此時間取得資料的話，這個區間的資料不會取得，反而是會取得下一個區間的資料，而天氣情況為3小一報
            //已就是說抓到的會是15:00~18:00的資料，不會是12:00~15:00
            //所以這邊的公式=(小時/3)*3，原因是先除以三後會取整數以14:33為例子
            //(14/3)*3=12，(14/3)*3+3=15，就可以得到需要的時間區間了
            String arrival_date = String.valueOf(calendar.get(Calendar.DATE));
            String arrival_hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
            String arrival_minute = String.valueOf(calendar.get(Calendar.MINUTE));
            String arrival_rangeMax, arrival_rangeMin;
            arrival_rangeMin = String.valueOf((Integer.valueOf(arrival_hour) / 3) * 3);
            arrival_rangeMax = String.valueOf(Integer.valueOf(arrival_rangeMin) + 3);
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(arrival_rangeMax));
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            //此Arrival_Time_rangeMax就是上述的(14/3)*3+3=15此值，而我們只需要帶入CWB open data的timeTo
            Arrival_Time_rangeMax = URLEncoder.encode(formatter.format(calendar.getTime()), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findViewByID(View view) {
        btn_bicycle = view.findViewById(R.id.btn_bicycle);
        btn_scooter = view.findViewById(R.id.btn_scooter);
        btn_car = view.findViewById(R.id.btn_car);
        btn_clear = view.findViewById(R.id.btn_clear);
        progressBar = view.findViewById(R.id.progressBar);
        tv_currentmode = view.findViewById(R.id.tv_currentmode);
        imagebtn_expandable = view.findViewById(R.id.imagebtn_expandable);
        linearLayout_expandContent = view.findViewById(R.id.linearLayout_expandContent);
        cardview = view.findViewById(R.id.cardview);
    }
}