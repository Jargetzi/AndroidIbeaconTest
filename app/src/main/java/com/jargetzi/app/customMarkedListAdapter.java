package com.jargetzi.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by michaellee on 2/10/14.
 */
public class customMarkedListAdapter  extends ArrayAdapter<iBeaconInfo>{
    protected static final String TAG = "RangingActivity";
    Context context;
    int layoutResourceId;
    List<iBeaconInfo> data = null;

    public customMarkedListAdapter(Context context, int layoutResourceId, List<iBeaconInfo> data) {
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

            holder.nickname = (TextView)row.findViewById(R.id.list_item_marked_nickname);
            holder.distance = (TextView)row.findViewById(R.id.list_item_marked_distance);
            row.setTag(holder);

        }
        else {
            holder = (iBeaconHolder)row.getTag();
        }

        iBeaconInfo info = data.get(position);

        holder.nickname.setText(info.nickname);
        holder.distance.setText(info.distance);

        if(info.getDistance().equals(context.getString(R.string.not_in_range_txt))) {
            row.setBackgroundColor(Color.YELLOW);
            row.invalidate();
        }

        return row;
    }

    static class iBeaconHolder {
        TextView nickname;
        TextView distance;
    }

}
