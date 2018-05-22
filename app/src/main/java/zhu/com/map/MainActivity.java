package zhu.com.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*实例化Client对象*/
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationLister());

        /*地图试图初始化的实例化*/
        SDKInitializer.initialize(getApplicationContext());

        /*加载布局*/
        setContentView(R.layout.activity_main);
        positionText = (TextView) findViewById(R.id.position_text_view);
        mapView = findViewById(R.id.bdmapView);

        /*获取BaiduMap类的实例*/
        baiduMap = mapView.getMap();

        /*让自己显示在地图上*/
        baiduMap.setMyLocationEnabled(true);


        /*对特殊权限进行动态注册*/
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);/*如果没获取quan'xian*/
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }


    /*请求位置信息*/
    private void requestLocation() {
        innitLocation();      /*当需要动态的实时刷新位置信息时调用这个方法*/
        mLocationClient.start();
    }

    /*实时动态刷新位置信息*/
    /* setLocationMode为选择定位方式
    * Hight_Accuracy --- 高分辨率 （默认方式）----优先使用GPS，没有GPS信号时使用网络定位
    * Battery_Saving --- 省电模式 ----  只使用网络定位
    * Device_Sensors ----传感器模式 --- 只使用GPS定位
    * */
    private void innitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);/*如果需要指定定位方式，使用这个方法*/
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

    }

    /*在activity的生命周期对mapview进行管理，保证资源及时释放*/
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();//当activity被销毁时，停止地图服务，防止它后台运行费电
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);//关闭显示我当前位置标识功能
    }

    /*请求权限的处理*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /*定位监听器*/
    private class MyLocationLister implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {

            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度 :").append(bdLocation.getLatitude()).append("\n");
                    currentPosition.append("经线 :").append(bdLocation.getLongitude()).append("\n");
                    currentPosition.append("国家 :").append(bdLocation.getCountry()).append("\n");
                    currentPosition.append("省 :").append(bdLocation.getProvince()).append("\n");
                    currentPosition.append("市 :").append(bdLocation.getCity()).append("\n");
                    currentPosition.append("区 :").append(bdLocation.getDistrict()).append("\n");
                    currentPosition.append("街道 :").append(bdLocation.getStreet()).append("\n");
                    currentPosition.append("定位方式 :");
                    if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                        currentPosition.append("GPS");
                    } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                        currentPosition.append("网络");
                    }
                    positionText.setText(currentPosition);
                }
            });
        }
    }

    private void navigateTo(BDLocation bdLocation) {
        if (isFirstLocation) {
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());//取出BDLocation的数据并封装到LatLng对象中
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);//将LatLng对象传入
            baiduMap.animateMapStatus(update);//将update对象传入animateMapStatus中作为参数实现缩放功能
            update = MapStatusUpdateFactory.zoomTo(16f);//缩放的比例
            baiduMap.animateMapStatus(update);
            isFirstLocation = false;//防止多次调用animateMapStatus（)方法的标志位
        }

        /*封装经纬度到 MyLocationData.Builder()中，实现我的当前定位位置标识*/
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder(); //MyLocationData.Builder()实例化
        locationBuilder.latitude(bdLocation.getLatitude());//将经纬度添加到MyLocationData.Builder()中
        locationBuilder.longitude(bdLocation.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);//将MyLocationData.Builder()传入
    }
}
