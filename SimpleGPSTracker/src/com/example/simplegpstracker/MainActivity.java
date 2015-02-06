package com.example.simplegpstracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.simplegpstracker.db.GPSInfoHelper;
import com.example.simplegpstracker.db.KalmanInfoHelperT;
import com.example.simplegpstracker.entity.GPSInfo;
import com.example.simplegpstracker.preference.PrefActivity;
import com.example.simplegpstracker.preference.PreferenceActivityP;
import com.example.simplegpstracker.utils.Utils;
import com.example.simplegpstracker.utils.UtilsNet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.preference.PreferenceManager;

public class MainActivity extends FragmentActivity {
	
	static final public String SATELLITE_COUNT= "com.example.simplegpstracker.satellitecount";
	static final public String LOCATION_ON= "com.example.simplegpstracker.locationon";
	//preferences
	private SharedPreferences preferences;
	private String provider;
	private String travelMode;
	private int refreshTime;
	private String status;
	private String routeName;
	
	private GoogleMap map;
	private SupportMapFragment mapFragment;
	private LocationLoader locationLoader;
	private Location location;
	
    ServiceConnection sConn;
    TrackService trackService;
    private int screenOrienrtation = 0;
    List<GPSInfo> list = null;
	
	private boolean isGPSEnabled;
	private boolean isNetworkEnabled;
	
	private BroadcastReceiver receiverProvider, reciverSatellite, receiverLocation ;
	
	private LatLng startPoint, endPoint;
	private PolylineOptions polylineOptions;

	Button bViewMap;
	Button bStartService;
	Button bStopService;
	Button bSendService;	
	
	ImageView ivMap;
	ImageView ivStartStop;
	ImageView ivSend;
	ImageView ivList;
	LinearLayout llListRoute;
	ListView listView;
	
	TextView tvProviders;
	TextView tvTravelMode;
	TextView tvRefreshTime;
	TextView tvGPSStatus;
	TextView tvNetworkStatus;
	TextView tvSatelliteCount;
	
	LinearLayout progressLayout;
	RouteAdapter routeAdapter;
	List<GPSInfo> listRoute;
	List<GPSInfo> listRoutePoints;
	
	GPSInfoHelper helper = null;
	private LocationManager locationManager;
	
	Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_map);
        
        context = getApplicationContext(); 
        list = new ArrayList<GPSInfo>();
        /*bViewMap = (Button)findViewById(R.id.bViewMap);
        bViewMap.setOnClickListener(new ClickListener());
        bStartService = (Button)findViewById(R.id.bStartService);
        bStartService.setOnClickListener(new ClickListener());
        bStopService = (Button)findViewById(R.id.bStopService);
        bStopService.setOnClickListener(new ClickListener());
        bSendService = (Button)findViewById(R.id.bSendServer);
        bSendService.setOnClickListener(new ClickListener());*/
        
        ivMap = (ImageView) findViewById(R.id.ivMap);
        ivMap.setOnClickListener(new ClickListener());
        ivStartStop = (ImageView) findViewById(R.id.ivRecord);
        ivStartStop.setOnClickListener(new ClickListener());
        ivSend = (ImageView) findViewById(R.id.ivSend);
        ivSend.setOnClickListener(new ClickListener());
        ivList = (ImageView) findViewById(R.id.ivList);
        ivList.setOnClickListener(new ClickListener());
        llListRoute = (LinearLayout) findViewById(R.id.llRouteList);
        listView = (ListView) findViewById(R.id.lvMyRoute);
        listView.setOnItemClickListener(new ListListener());
        registerForContextMenu(listView);
        
        /*tvProviders = (TextView) findViewById(R.id.tvProvidersValue);
        tvTravelMode = (TextView) findViewById(R.id.tvTravelModeValue);
        tvRefreshTime = (TextView) findViewById(R.id.tvRefreshTimeValue);
        tvGPSStatus = (TextView) findViewById(R.id.tvGPSValue);
        tvNetworkStatus = (TextView) findViewById(R.id.tvNetworkValue);*/
        tvSatelliteCount = (TextView) findViewById(R.id.tvSatelliteCount);
       // progressLayout = (LinearLayout) findViewById(R.id.progressLayoutMain);
        
       /* if(UtilsNet.IsServiceRunning(context)) progressLayout.setVisibility(View.VISIBLE);
        else progressLayout.setVisibility(View.INVISIBLE);*/
        
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);	
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
		map = mapFragment.getMap();  

		startPoint = null;
    	locationLoader = new LocationLoader(context);
    	location = locationLoader.getLocation();
		setMapView();
        
    	//This receiver receive provider status for update MainActivity info 
    	receiverProvider = new BroadcastReceiver()
        {   
        @Override
          public void onReceive( Context context, Intent intent )
          {
            //Get status provider  
        	getStatus();
            //Update views on MainActivity
        	//setStatusGPS();
            //setStatusNetwork();
        	setMapView();
          }
        };
            
        //This receiver receive satellites count for update MainActivity info 
        reciverSatellite = new BroadcastReceiver()
        {   
        @Override
          public void onReceive( Context context, Intent intent )
          {
              int satelliteCount = intent.getIntExtra("count", 0);
              LatLng point = new LatLng(intent.getDoubleExtra("lat", 0.0),
            		  					intent.getDoubleExtra("lng", 0.0));
              drawPoly(point, Color.RED);
              Log.i("DEBUG:", "point" + "lat: " + point.latitude + "lon:" + point.longitude);
              tvSatelliteCount.setText(String.valueOf(satelliteCount));
          }
        };
        
        context.registerReceiver(receiverProvider, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        context.registerReceiver(reciverSatellite, new IntentFilter(SATELLITE_COUNT));
        
        sConn = new ServiceConnection() {       	
		      		    
		    public void onServiceDisconnected(ComponentName name) {
		        Log.d("DEBUG:", "MainActivity onServiceDisconnected");
		    }

			@Override
			public void onServiceConnected(ComponentName component, IBinder binder) {
				 Log.i("DEBUG", "MainActivity onServiceConnected");
			        trackService = ((TrackService.ServiceBinder) binder).getService(); 
			
			        //if (isRunning) {
			        if (screenOrienrtation == 0){						//loading a data if a device orientation has not been changed
			        													// 1 // Load a data from DB

			        													// 2 // after load The data from Net if a device is online
			        	if (UtilsNet.isOnline(context)){
			        		
			        	}

			        } else {											//loading the data if the device orientation has been changed

			        }
				
			}			
		 };		    
        
    }
    
    private void setMapView(){

    	getStatus();
    	if(isGPSEnabled | isNetworkEnabled){
    		//get current location
    		location = locationLoader.getLocation();
    		
    		if(location != null){
    			map.setIndoorEnabled(true);
    			map.setMyLocationEnabled(true);
    			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),15));
    		 //map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(
    			       // BitmapDescriptorFactory.defaultMarker()));
    		}
    	}
    }
    
    private void drawPoly(LatLng point, int color){
   		endPoint = point;
   		Log.i("DEBUG:", "point" + "lat: " + point.latitude + "lon:" + point.longitude);
    	if(startPoint != null){
    		map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(point.latitude, point.longitude)));
    		polylineOptions = new PolylineOptions().width(3).color(color);
    		polylineOptions.add(startPoint, endPoint);
        	map.addPolyline(polylineOptions);
    	}
    	startPoint = endPoint;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            	Intent intentPref = new Intent(this, PrefActivity.class);
            	startActivity(intentPref);
            } else {
            	Intent intentPref = new Intent(this, PreferenceActivityP.class);
            	startActivity(intentPref);
            }
        	
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {

    	super.onCreateContextMenu(menu, v, menuInfo);
		if(v.getId() == R.id.lvMyRoute){
			 MenuInflater inflater = getMenuInflater();
			 inflater.inflate(R.menu.menu_list, menu);
		}
 
     }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
          switch(item.getItemId()) {
              case R.id.edit:
                // edit stuff here
                    return true;
              case R.id.delete:
            String selectedName = listRoute.get(info.position).getName();
            helper.deleteRow(selectedName);
            refreshAdapter();
            //routeAdapter.notifyDataSetChanged();
                    return true;
              default:
                    return super.onContextItemSelected(item);
          }
    }
    
    private void refreshAdapter() {
		listRoute = new ArrayList<GPSInfo>();
		helper = new GPSInfoHelper(context);
		listRoute = helper.getGPSPointRoute();
		routeAdapter = new RouteAdapter(listRoute, context);
		listView.setAdapter(routeAdapter);
	}
    
    
    private void getPreferences(){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        provider = preferences.getString("providers", "Network");
        travelMode = preferences.getString("travelMode", "walking");
        refreshTime = Integer.parseInt(preferences.getString("refreshTime", "5"));        
    }
    
    private void getStatus(){

        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i("DEBUG:", "provider GPS" + isGPSEnabled);
    	isNetworkEnabled = locationManager
    			.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    	Log.i("DEBUG:", "provider Net" + isNetworkEnabled);
    }
    
    private void setStatusGPS(){
    	if(isGPSEnabled) {
        	//tvGPSStatus.setText(getResources().getString(R.string.status_enabled));
        	//tvGPSStatus.setTextColor(Color.GREEN);
        }
        else if(!isGPSEnabled) {
        	//tvGPSStatus.setText(getResources().getString(R.string.status_disabled));
        	//tvGPSStatus.setTextColor(Color.BLACK);
        }
        
    }
    
    private void setStatusNetwork(){   	
        if(isNetworkEnabled) {
        	//tvNetworkStatus.setText(getResources().getString(R.string.status_enabled));
        	//tvNetworkStatus.setTextColor(Color.GREEN);
        }
        else if(!isNetworkEnabled) {
        	//tvNetworkStatus.setText(getResources().getString(R.string.status_disabled));
        	//tvNetworkStatus.setTextColor(Color.BLACK);
        }
    }
    
    protected void onResume(){
    	super.onResume();
        getPreferences();
        getStatus();
        
        //tvProviders.setText(provider);
        //tvTravelMode.setText(travelMode);
        //tvRefreshTime.setText(String.valueOf(refreshTime));
        
        //setStatusGPS();
       // setStatusNetwork();
    }

    private class ClickListener implements OnClickListener{

		@Override
		public void onClick(View view) {
			int id = view.getId();
			switch (id) {
			/*case R.id.bStartService:
				Intent iStartService = new Intent(context, TrackService.class);
				startService(iStartService);
				Toast toast_start = Toast.makeText(context, context.getResources().getString(R.string.service_start), Toast.LENGTH_SHORT); 
				toast_start.show(); 
				if(isGPSEnabled | isNetworkEnabled)
				progressLayout.setVisibility(View.VISIBLE);
				break;
			case R.id.bStopService:
				Intent iStopService = new Intent(context, TrackService.class);
				stopService(iStopService);
				progressLayout.setVisibility(View.INVISIBLE);
				break;
			case R.id.bViewMap:
				if(!UtilsNet.isOnline(getApplicationContext())){
					Toast toast = Toast.makeText(context, context.getResources().getString(R.string.network_off), Toast.LENGTH_SHORT); 
					toast.show();				
				}else if(UtilsNet.IsServiceRunning(context)){
					Toast toast = Toast.makeText(context, context.getResources().getString(R.string.service_started), Toast.LENGTH_SHORT); 
					toast.show();
				}else{
					Intent iMap = new Intent(context, ViewMapActivity.class);
					startActivity(iMap);
				}
				break;
			case R.id.bSendServer:
				//new Transmitter(context).send();
				Intent tMap = new Intent(context, TestMap.class);
				startActivity(tMap);
				break;*/
			case R.id.ivRecord:
				/*if(!UtilsNet.IsServiceRunning(context)){
					ivStartStop.setImageResource(R.drawable.stop_selector);
					Intent iStartService = new Intent(context, TrackService.class);
					startService(iStartService);
					
					Toast toast_start = Toast.makeText(context, context.getResources().getString(R.string.service_start), Toast.LENGTH_SHORT); 
					toast_start.show(); 
				}else if(UtilsNet.IsServiceRunning(context)){
					ivStartStop.setImageResource(R.drawable.record_selector);
					Intent iStopService = new Intent(context, TrackService.class);
					stopService(iStopService);
				}*/
				//check if service is running (no for IntentServise)
				Intent iStartService = new Intent(context, TrackService.class);
		        if (!UtilsNet.IsServiceRunning(context)) {
		            // Bind to LocalService	
		        	ivStartStop.setImageResource(R.drawable.stop_selector);
		            bindService(iStartService, sConn, getApplicationContext().BIND_AUTO_CREATE);
		            Toast toast_start = Toast.makeText(context, context.getResources().getString(R.string.service_start), Toast.LENGTH_SHORT); 
					toast_start.show(); 
		        }
		        else if(UtilsNet.IsServiceRunning(context)){
		        	ivStartStop.setImageResource(R.drawable.record_selector);
		        	
		        	//get obtained a rout data to be stored in the database 
		        	list = trackService.getList();
		        	trackService.stop();        	
		        	unbindService(sConn);
		        	
		        	//???? maybe it is not necessary to do
		        	//locationLoader.Unregister();
		        	
		        	if(list != null && list.size() > 2){
		        		new DialogSaveRoute().show(getSupportFragmentManager(), "DialogSaveRoute");
		        	}
		        }		        		        
				break;
			case R.id.ivMap:
				/*if(!UtilsNet.isOnline(getApplicationContext())){
					Toast toast = Toast.makeText(context, context.getResources().getString(R.string.network_off), Toast.LENGTH_SHORT); 
					toast.show();				
				}else if(UtilsNet.IsServiceRunning(context)){
					Toast toast = Toast.makeText(context, context.getResources().getString(R.string.service_started), Toast.LENGTH_SHORT); 
					toast.show();
				}else{
					Intent iMap = new Intent(context, ViewMapActivity.class);
					startActivity(iMap);
				}*/
				new DialogSaveRoute().show(getSupportFragmentManager(), "DialogSaveRoute");
				break;
			case R.id.bSendServer:
				new Transmitter(context).send();				
				break;	
			case R.id.ivList:
				if(!llListRoute.isShown()) {
					refreshAdapter();
					llListRoute.setVisibility(View.VISIBLE);	
					listView.setVisibility(View.VISIBLE);

				}
				else llListRoute.setVisibility(View.INVISIBLE);		
				
				
				break;	
				
			default:
				break;
			}
		
		}
    	
    }   
    
    public void SaveRoute(String name){
    	
    	helper = new GPSInfoHelper(context);
    	routeName = name;
    	
    	if(name.isEmpty()){
    		routeName = "Route name: " + String.valueOf(Utils.getTimeForName(list.get(0).getTime()));
    	}   		
    	
    	for(GPSInfo info: list){
			info.setName(routeName);
			helper.insert(info);
		}
    	
    	
    }
    
    private class ListListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			map.clear();
			startPoint = null;
			listRoutePoints = helper.getRoutePoints(listRoute.get(position).getName());
			//Log.i("DEBUG:", "route name:" + listRoute.get(position).getName());
			llListRoute.setVisibility(View.INVISIBLE);	
			LatLng point = null;
	    	for(GPSInfo info: listRoutePoints){
	    		point = new LatLng(info.getLatitude(), info.getLongitude());
	    		drawPoly(point, Color.RED);
	    	}
		}
    }

    
    private void routeLooper(List<GPSInfo> list, int color){
    	LatLng point = null;
    	for(GPSInfo info: list){
    		point = new LatLng(info.getLatitude(), info.getLongitude());
    		drawPoly(point, color);
    	}
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reciverSatellite);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverProvider);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(helper != null) helper.closeDB();
    	locationLoader.Unregister();
    }

}
