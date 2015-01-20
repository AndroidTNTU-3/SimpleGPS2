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
import com.example.simplegpstracker.kalman.KalmanManagerC;
import com.example.simplegpstracker.kalman.KalmanMen;
import com.example.simplegpstracker.kalman.Tracker3D;

public class ToKalmanC {
	
	KalmanManagerC km;
	Location location;
	List<GPSInfo> list;
	GPSInfo kalmanInfo;
	KalmanInfoHelperT kalmanHelper;
	Context context;
	
	ToKalmanC(List<GPSInfo> list, Context context){
		location = new Location(KalmanManager.KALMAN_PROVIDER);
		//km = new KalmanManager(context);
		km = new KalmanManagerC(context);
		this.list = list;
		this.context = context;
		kalmanHelper = new KalmanInfoHelperT(context);
        kalmanHelper.cleanOldRecords();
       
	}
	
	public void compute(){
		kalmanInfo = new GPSInfo();

		for(GPSInfo info: list){
			//if(info.getAccuracy() < 5){
				Log.i("DEBUG", "accuracy:" + info.getAccuracy());
				fromKalmanManager(info);							
			//}
		}				
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

}
