package com.example.simplegpstracker.kalman;

import android.location.Location;

public class KalmanMen {
	private Location location;
	private Tracker3D mLatitudeTracker, mLongitudeTracker;
	public static final String KALMAN_PROVIDER = "kalman";
	
	public void setParam(Location location){
		this.location = location;
		double position, noise;
		
		double accuracy = location.getAccuracy();
		position = location.getLatitude();
        //noise = accuracy * METER_TO_DEG;
		noise = accuracy;

        if (mLatitudeTracker == null) {

        	mLatitudeTracker = new Tracker3D(5, noise);
            mLatitudeTracker.setState(location.getLatitude(), location.getSpeed());
        }
        
        mLatitudeTracker.Update(location.getLatitude(), noise, location.getSpeed());
        
        position = location.getLongitude();
        //noise = accuracy * METER_TO_DEG;
		noise = accuracy;

        if (mLongitudeTracker == null) {

        	mLongitudeTracker = new Tracker3D(5, noise);
        	mLongitudeTracker.setState(location.getLongitude(), location.getSpeed());
        }
        
        mLongitudeTracker.Update(location.getLongitude(), noise, location.getSpeed());
		
	}
	
	public Location getKalmanLocation(){
		final Location location = new Location(KALMAN_PROVIDER);
		
		location.setLatitude(mLatitudeTracker.getPosition());

        location.setLongitude(mLongitudeTracker.getPosition());
		return location;
	}

}
