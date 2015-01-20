package com.example.simplegpstracker;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.simplegpstracker.db.KalmanInfoHelperT;
import com.example.simplegpstracker.entity.GPSInfo;
import com.example.simplegpstracker.kalman.GeoTrackFilter;
import com.example.simplegpstracker.kalman.KalmanFilter1;
import com.example.simplegpstracker.kalman.KalmanManager;
import com.example.simplegpstracker.kalman.KalmanMen;
import com.example.simplegpstracker.kalman.Tracker3D;

public class ToKalmanGeoTrack {
	private static final double DEG_TO_METER = 111225.0;
	private static final double METER_TO_DEG = 1.0 / DEG_TO_METER;
    private static final double COORDINATE_NOISE = 3.0 * METER_TO_DEG;
	
	Location location;
	List<GPSInfo> list;
	GPSInfo kalmanInfo;
	KalmanInfoHelperT kalmanHelper;
	Context context;
	GeoTrackFilter geoTrackFilter;
	double[] lat_lon;
	
	private double timeStepShared;
	private SharedPreferences preferences;
	
	ToKalmanGeoTrack(List<GPSInfo> list, Context context){
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		//how to show the route on a map
		timeStepShared = (double)Integer.parseInt(preferences.getString("refreshTime", "5"));
		
		lat_lon = new double[]{0.0, 0.0};
		location = new Location(LocationManager.GPS_PROVIDER);
		this.list = list;
		this.context = context;
		kalmanHelper = new KalmanInfoHelperT(context);
        kalmanHelper.cleanOldRecords();
        geoTrackFilter = new GeoTrackFilter(COORDINATE_NOISE, timeStepShared);
       
	}
	
	public void compute(){
		kalmanInfo = new GPSInfo();

		for(GPSInfo info: list){
			//if(info.getAccuracy() < 5){
				Log.i("DEBUG", "accuracy:" + info.getAccuracy());
				fromGeoTrack(info);					
			//}
		}
				
	}
	
	private void fromGeoTrack(GPSInfo info){
		geoTrackFilter.update_velocity2d(info.getLatitude(), info.getLongitude(), timeStepShared);
 		lat_lon = geoTrackFilter.get_lat_long();
 		kalmanInfo.setLongitude(lat_lon[1]);
 		kalmanInfo.setLatitude(lat_lon[0]);
 		kalmanInfo.setBearing(geoTrackFilter.get_bearing());
 		kalmanHelper.insert(kalmanInfo);
	}
	
}
