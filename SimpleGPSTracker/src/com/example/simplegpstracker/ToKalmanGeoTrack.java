package com.example.simplegpstracker;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.example.simplegpstracker.db.KalmanInfoHelperT;
import com.example.simplegpstracker.entity.GPSInfo;
import com.example.simplegpstracker.kalman.GeoTrackFilter;
import com.example.simplegpstracker.kalman.KalmanFilter1;
import com.example.simplegpstracker.kalman.KalmanManager;
import com.example.simplegpstracker.kalman.KalmanMen;
import com.example.simplegpstracker.kalman.Tracker3D;

public class ToKalmanGeoTrack {
	
	KalmanManager km;
	Location location;
	List<GPSInfo> list;
	GPSInfo kalmanInfo;
	KalmanInfoHelperT kalmanHelper;
	Context context;
	KalmanFilter1 kalmanFilter;
	GeoTrackFilter geoTrackFilter;
	double[] lat_lon;
	KalmanMen kalmanMen;
	
	ToKalmanGeoTrack(List<GPSInfo> list, Context context){
		lat_lon = new double[]{0.0, 0.0};
		location = new Location(LocationManager.GPS_PROVIDER);
		km = new KalmanManager(context);
		kalmanMen = new KalmanMen();
		this.list = list;
		this.context = context;
		kalmanHelper = new KalmanInfoHelperT(context);
        kalmanHelper.cleanOldRecords();
        kalmanFilter = new KalmanFilter1();
        geoTrackFilter = new GeoTrackFilter(2);
       
	}
	
	public void compute(){
		kalmanInfo = new GPSInfo();

		for(GPSInfo info: list){
			//if(info.getAccuracy() < 5){
				Log.i("DEBUG", "accuracy:" + info.getAccuracy());
				/*Location kalmanLocation = new Location(KalmanManager.KALMAN_PROVIDER);
				kalmanLocation = kalmanFilter.process(info.getSpeed(), info.getLatitude(), info.getLongitude(), 5, info.getAccuracy());*/
				//fromKalmanMan(info);
				//fromKalmanManager(info);
				fromGeoTrack(info);
				
				/*if(kalmanLocation != null){
				kalmanInfo.setId(1);
		 		kalmanInfo.setLongitude(kalmanLocation.getLongitude());
		 		kalmanInfo.setLatitude(kalmanLocation.getLatitude());
		 		//kalmanInfo.setAccuracy(kalmanLocation.getAccuracy());
		 		//kalmanInfo.setAcceleration(kalmanLocation.getAcceleration());
		 		kalmanInfo.setTitle("Track1");
		 		kalmanInfo.setTime(System.currentTimeMillis());
		 		kalmanHelper.insert(kalmanInfo);
				}*/
				
			
			//}
		}
				
	}
	
	private void fromGeoTrack(GPSInfo info){
		geoTrackFilter.update_velocity2d(info.getLatitude(), info.getLongitude(), 5);
 		lat_lon = geoTrackFilter.get_lat_long();
 		kalmanInfo.setLongitude(lat_lon[1]);
 		kalmanInfo.setLatitude(lat_lon[0]);
 		kalmanHelper.insert(kalmanInfo);
	}
	
	private void fromKalmanManager(GPSInfo info) {
		
		Location kalmanLocation = new Location(KalmanManager.KALMAN_PROVIDER);
		location.setLatitude(info.getLatitude());
		location.setLongitude(info.getLongitude());
		location.setAccuracy(info.getAccuracy());
		location.setSpeed(info.getSpeed());
		km.setParam(location);
 		kalmanLocation = km.getKalmanLocation();
 		
 		kalmanInfo.setId(1);
 		kalmanInfo.setLongitude(kalmanLocation.getLongitude());
 		kalmanInfo.setLatitude(kalmanLocation.getLatitude());
 		kalmanInfo.setAccuracy(kalmanLocation.getAccuracy());
 		//kalmanInfo.setAcceleration(kalmanLocation.getAcceleration());
 		kalmanInfo.setTitle("Track1");
 		kalmanInfo.setTime(System.currentTimeMillis());
 		kalmanHelper.insert(kalmanInfo);
		
	}

	private void fromKalmanMan(GPSInfo info){
		location.setLatitude(info.getLatitude());
		location.setLongitude(info.getLongitude());
		location.setAccuracy(info.getAccuracy());
		location.setSpeed(info.getSpeed());
		kalmanMen.setParam(location);
		Location kalmanLocation = new Location(KalmanManager.KALMAN_PROVIDER);
 		kalmanLocation = kalmanMen.getKalmanLocation();
 		kalmanInfo.setId(1);
 		kalmanInfo.setLongitude(kalmanLocation.getLongitude());
 		kalmanInfo.setLatitude(kalmanLocation.getLatitude());
 		kalmanHelper.insert(kalmanInfo);
	}
}
