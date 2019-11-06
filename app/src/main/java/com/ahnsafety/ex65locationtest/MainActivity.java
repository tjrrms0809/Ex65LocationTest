package com.ahnsafety.ex65locationtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tvProviders;
    TextView tvBestProvider;
    TextView tvMyLocation;
    TextView tvAutoMyLocation;

    LocationManager locationManager;

    boolean isEnter= false;//특정지점 안에 있는가?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvProviders = findViewById(R.id.tv_providers);
        tvBestProvider = findViewById(R.id.tv_bestprovider);
        tvMyLocation = findViewById(R.id.tv_mylocation);
        tvAutoMyLocation= findViewById(R.id.tv_automylocation);

        //위치정보관리자 객체 소환하기
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //위치정보제공자(location provider)
        //1. gps : 가장 높은 정확도, 무료, 실내에서 사용불가, 기상영향 받음, 베터리 소모 많음
        //2. network(wifi, 3g, 4g, lte, 5g) : 중간정도 정확도, 유료, 어디서 가능, 베터리 소모 중간
        //3. passive : 다른 앱의 마지막 위치정보를 사용. 정확도 가장 낮음, 베터리 소모 적음, 사용빈도 거의 없음.

        //디바이스에서 사용가능한 위치정보 제공자를 확인
        List<String> providers = locationManager.getAllProviders();
        String s = "";
        for (String provider : providers) {
            s += provider + ", ";
        }
        tvProviders.setText(s);

        //위치정보 제공자 중에서 최고의 제공자 판별 요청
        //최고의 제공자를 판별하기위한 기준(criteria) 객체
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);//비용지불 감수
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);//정확도를 요하는가?
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);//베터리 소모량
        criteria.setAltitudeRequired(true);//고도에 대한 위치 필요한가?

        //베스트 위치정보를 얻으려면 퍼미션필요
        String bestProvider = locationManager.getBestProvider(criteria, true);
        tvBestProvider.setText(bestProvider);

        //마시멜로우(api23버전)부터 동적 퍼미션 필요 : 앱을 다운로드할 때 뿐만아니라, 사용할 때도 퍼미션을 체크하는 방식
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션이 허가되어 있지 않은지?
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //퍼미션을 요청하는 다이얼로그 보이기..
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, 10);//다이얼로그를 보여주는 메소드
            }
        }

    }//onCreate Method..

    //requestPermissions()메소드의 호출을 통해
    //보여진 다이얼로그의 선택이 종료되면
    //자동으로 실행되는 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 10:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "위치정보제공에 동의하셨습니다.", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    public void clickBtn(View view) {

        //퍼미션 체크..안되어 있으면 하지마!!
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        //현재 내 위치 정보 얻어오기
        Location location = null;

        //매니저를 이용하여 위치정보 얻어오기
        if (locationManager.isProviderEnabled("gps")) {
            location = locationManager.getLastKnownLocation("gps");
        }else if( locationManager.isProviderEnabled("network")){
            location = locationManager.getLastKnownLocation("network");
        }

        if(location==null){
            tvMyLocation.setText("내 위치 못 찾았어!!");
        }else{
            //위도, 경도 얻어오기
            double latitude= location.getLatitude();
            double longitude= location.getLongitude();

            tvMyLocation.setText(latitude+" , "+ longitude);
        }

    }

    public void clickBtn2(View view) {
        //퍼미션 체크..안되어 있으면 하지마!!
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        //내 위치 자동갱신하도록 ..
        if(locationManager.isProviderEnabled("gps")){
            locationManager.requestLocationUpdates("gps", 5000, 2, locationListener);
        }else if(locationManager.isProviderEnabled("network")){
            locationManager.requestLocationUpdates("network", 5000, 2, locationListener);
        }
    }

    public void clickBtn3(View view) {
        //퍼미션 체크..안되어 있으면 하지마!!
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //내 위치 자동갱신 제거
        locationManager.removeUpdates(locationListener);
    }


    //위치정보 갱신을 듣는 리스너 멤버변수
    LocationListener locationListener= new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            double latitude= location.getLatitude();
            double longitude= location.getLongitude();

            tvAutoMyLocation.setText(latitude+", "+longitude);


            //// 특정 지점에 들어갔을 때 이벤트 발생/////////
            //왕십리역 좌표 : 37.561197, 127.038018

            //내 위치(latitude, longitude)와 왕십리역 사이의 실제거리(m)
            float[] result= new float[3];//거리계산 결과를 저장할 배열객체
            Location.distanceBetween(latitude, longitude, 37.561197, 127.038018, result);

            //result[0]에 두 좌표사의의 m거리가 계산되어 저장되어 있음.
            if( result[0]<50 ){ //두 좌표거리가 50m이내 인가?
                if(isEnter==false){
                    new AlertDialog.Builder(MainActivity.this).setMessage("축하합니다. 이벤트 달성!!").setPositiveButton("OK", null).create().show();
                    isEnter=true;
                }
            }else{
                isEnter= false;
            }



        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


}//MainActivity class...
