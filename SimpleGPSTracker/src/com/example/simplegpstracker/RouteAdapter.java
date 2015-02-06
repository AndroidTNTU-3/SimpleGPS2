package com.example.simplegpstracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.example.simplegpstracker.entity.GPSInfo;
import com.example.simplegpstracker.utils.Utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RouteAdapter extends BaseAdapter {
	
	private List<GPSInfo> listRoute;
	private Context context;
	
	
	RouteAdapter(List<GPSInfo> listRoute, Context context){
		this.listRoute = listRoute;
		this.context= context;
	}

	@Override
	public int getCount() {
		return listRoute.size();
	}

	@Override
	public Object getItem(int position) {	
		return listRoute.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			convertView = LayoutInflater.from(context).inflate(
					R.layout.route_list_row, parent, false);
			
			
			TextView name = (TextView) convertView.findViewById(R.id.tvRouteName);
			TextView startTime = (TextView) convertView.findViewById(R.id.tvRouteStartTime);
			//TextView stopTime = (TextView) convertView.findViewById(R.id.text_item);
			
			
			ViewHolder vh = new ViewHolder(name, startTime);
			
			convertView.setTag(vh);
			
		}

		
		ViewHolder vh = (ViewHolder) convertView.getTag();
		
		vh.name.setText(listRoute.get(position).getName());
		vh.startTime.setText(Utils.getWideTimeFormat(listRoute.get(position).getTime()));
		
		

		return convertView;
	}
	
	
	
	private class ViewHolder{
		public final TextView name;
		public final TextView startTime;

		
		public ViewHolder (TextView name, TextView startTime){
			this.name = name;
			this.startTime= startTime;
		}
		
		
	}
	
	

}
