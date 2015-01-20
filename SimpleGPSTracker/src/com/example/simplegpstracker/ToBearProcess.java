package com.example.simplegpstracker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.simplegpstracker.PointAdapter.PointAdapterCallBack;
import com.example.simplegpstracker.db.KalmanInfoHelperT;
import com.example.simplegpstracker.db.ProcessedInfoHelper;
import com.example.simplegpstracker.entity.GPSInfo;
import com.example.simplegpstracker.kalman.KalmanManager;
import com.google.android.gms.maps.model.LatLng;

public class ToBearProcess{
	
	Location location;
	List<GPSInfo> list;
	GPSInfo info;
	private ProcessedInfoHelper processedHelper;
	PointAdapter pointAdapter;
	Context context;
	Location startingLocation = new Location("starting point");
    Location endingLocation = new Location("ending point");
    LatLng tempPoint = null;
    double bearing;
    
	ToBearProcess(List<GPSInfo> list, Context context){
		info = new GPSInfo();
		location = new Location(KalmanManager.KALMAN_PROVIDER);
		this.list = list;
		this.context = context;
		processedHelper = new ProcessedInfoHelper(context);
		processedHelper.cleanOldRecords();
		insertInfo(list);
	}
	
	private void insertInfo(List<GPSInfo> list){
		for(GPSInfo i: list){
			info.setLatitude(i.getLatitude());
			info.setLongitude(i.getLongitude());
			LatLng point = new LatLng(i.getLatitude(), i.getLongitude());
			info.setBearing(getBearing(point));
			processedHelper.insert(info);
		}
	}
	
	private double getBearing(LatLng point){
		if(tempPoint == null) {
			tempPoint = point;
			return 0.0;
		}
		else{
		    startingLocation.setLatitude(tempPoint.latitude);
		    startingLocation.setLongitude(tempPoint.longitude);
		    
		    endingLocation.setLatitude(point.latitude);
		    endingLocation.setLongitude(point.longitude);
		    
		    bearing = startingLocation.bearingTo(endingLocation);
		    tempPoint = point;
			Log.i("DEBUG:", "Bearing:" + String.valueOf(bearing));
			return bearing;

		}
		
	}
	
	
}
