package com.tangojlabs.locationdata;

/**
 * Created by Sean on 10/1/2015.
 */
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListViewAdapter extends ArrayAdapter<DataPoint> {
    private static final String TAG = "ListViewAdapter";

    public ListViewAdapter(Context context, ArrayList<DataPoint> dataPoints) {
        super(context, 0, dataPoints);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DataPoint dataPoint = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_row_columns, parent, false);
        }
        TextView txtFirst = (TextView) convertView.findViewById(R.id.date_time);
        TextView txtSecond = (TextView) convertView.findViewById(R.id.azimuth);
        TextView txtThird = (TextView) convertView.findViewById(R.id.lat);
        TextView txtFourth = (TextView) convertView.findViewById(R.id.lng);

        txtFirst.setText(dataPoint.dateTime);
        txtSecond.setText(Float.toString(dataPoint.azimuth) + (char) 0x00B0);
        txtThird.setText("Lat: " + String.format("%.5f", dataPoint.lat));
        txtFourth.setText("Lng: " + String.format("%.5f", dataPoint.lng));

        return convertView;
    }
}
