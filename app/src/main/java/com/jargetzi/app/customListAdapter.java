package com.jargetzi.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by michaellee on 2/10/14.
 */
public class customListAdapter  extends ArrayAdapter<iBeaconInfo>{
    protected static final String TAG = "RangingActivity";
    Context context;
    int layoutResourceId;
    List<iBeaconInfo> data = null;

    public customListAdapter(Context context, int layoutResourceId, List<iBeaconInfo> data) {
        super(context,layoutResourceId,data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        iBeaconHolder holder;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId,parent,false);

            holder = new iBeaconHolder();
            holder.uuid = (TextView)row.findViewById(R.id.list_item_uuid);
            holder.distance = (TextView)row.findViewById(R.id.list_item_distance);
            holder.major = (TextView)row.findViewById(R.id.list_item_major);
            holder.minor = (TextView)row.findViewById(R.id.list_item_minor);

            row.setTag(holder);
        }
        else {
            holder = (iBeaconHolder)row.getTag();
        }

        iBeaconInfo info = data.get(position);

        holder.uuid.setText(info.uuid);
        holder.distance.setText(info.distance);
        holder.major.setText(info.major);
        holder.minor.setText(info.minor);

        return row;
    }

    static class iBeaconHolder {
        TextView uuid;
        TextView distance;
        TextView major;
        TextView minor;
    }

}
