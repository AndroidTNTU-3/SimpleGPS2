package com.example.simplegpstracker.kalman;

import android.content.Context;

public class FactoryKalman {
	
	public static final int KALMAN_MANGER = 0;
	public static final int KALMAN_MANGER_C = 1;
	public static final int KALMAN_MANGER_GEO = 2;
	
	public interface IKalmanManager{
		
	}
	
	/*public IKalmanManager getFactory(int manager_key, Context context){
		
		switch(manager_key){
		case 0:
			return new KalmanFactoryT();
		break;
		case 1:
			return new KalmanFactoryC();
		break;
		case 1:
			return new KalmanFactoryGeo();
		break;
		default:
			return null;
		}
		
	}*/

}
