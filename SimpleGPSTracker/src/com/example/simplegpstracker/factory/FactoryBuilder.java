package com.example.simplegpstracker.factory;

import com.example.simplegpstracker.kalman.KalmanManager;
import com.example.simplegpstracker.kalman.KalmanManagerC;
import com.example.simplegpstracker.kalman.KalmanManagerGeo;
import com.example.simplegpstracker.kalman.KalmanManagerSimple;

import android.content.Context;

public class FactoryBuilder {
	
	public static final String KALMAN_VILLOREN= "Villoren";
	public static final String KALMAN_GEOTRACK= "GeoTrack";
	public static final String KALMAN_PORT_C= "PortC";
	public static final String KALMAN_SIMPLE= "Simple";
	
	public static FactoryKalmanBuilder getFactory(String type, Context context){
		
		if(type.equals(KALMAN_VILLOREN)) return new FactoryVilloren(new KalmanManager(context));
		else if (type.equals(KALMAN_PORT_C)) return new FactoryPortC(new KalmanManagerC(context));
		else if (type.equals(KALMAN_GEOTRACK)) return new FactoryGeo(new KalmanManagerGeo(context));
		else if (type.equals(KALMAN_SIMPLE)) return new FactorySimple(new KalmanManagerSimple(context));
		return null;
	}

}
