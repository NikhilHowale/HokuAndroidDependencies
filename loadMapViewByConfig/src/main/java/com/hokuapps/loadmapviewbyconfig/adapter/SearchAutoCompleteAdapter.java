package com.hokuapps.loadmapviewbyconfig.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hokuapps.loadmapviewbyconfig.R;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class SearchAutoCompleteAdapter extends ArrayAdapter<JSONObject> implements Filterable {

    private Context context;
    private int textViewResourceId;

    private JSONArray searchArrJson;
    private JSONArray mOriginalSearchArrJson;
    private TextView tvAddress;

    private String keyToBind;

    public SearchAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        mOriginalSearchArrJson = new JSONArray();
    }


    public void setSearchArrJson(JSONArray searchArrJson) {
        this.searchArrJson = searchArrJson;
        this.mOriginalSearchArrJson = searchArrJson;

    }

    public void setKeyToBind(String keyToBind) {
        this.keyToBind = keyToBind;
    }

    @Override
    public JSONObject getItem(int index) {
        try {
            return searchArrJson.getJSONObject(index);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        tvAddress = (TextView) convertView.findViewById(android.R.id.text1);

        tvAddress.setText(Utility.getStringObjectValue(getItem(position), keyToBind));

        return convertView;
    }

    @Override
    public int getCount() {
        if(searchArrJson != null)
            return searchArrJson.length();
        else
            return 0;
    }

    private JSONArray autocomplete(String input) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < mOriginalSearchArrJson.length(); i++) {

                JSONObject jsonObject = mOriginalSearchArrJson.getJSONObject(i);

                if(Objects.requireNonNull(Utility.getStringObjectValue(jsonObject, keyToBind)).toLowerCase().contains(input.toLowerCase())) {
                    jsonArray.put(jsonObject);
                }

            }
        } catch (JSONException e) {
            Log.e("", context.getString(R.string.cannot_process_json), e);
        }

        return jsonArray;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    searchArrJson = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = searchArrJson;
                    filterResults.count = searchArrJson.length();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
