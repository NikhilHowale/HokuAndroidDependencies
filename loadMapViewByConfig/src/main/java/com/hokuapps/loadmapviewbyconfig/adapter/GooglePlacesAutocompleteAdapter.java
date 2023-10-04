package com.hokuapps.loadmapviewbyconfig.adapter;

import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.MAIN_TEXT;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.PLACE_ID;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.PREDICTIONS;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SECONDARY_TEXT;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.STRUCTURED_FORMATTING;

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
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;
import com.hokuapps.loadmapviewbyconfig.models.PlaceModel;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by admin on 03/29/18.
 */

public class GooglePlacesAutocompleteAdapter extends ArrayAdapter<PlaceModel> implements Filterable {
    private static final String CLASS_TAG = GooglePlacesAutocompleteAdapter.class.getSimpleName();
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    String countryArray = "";
    private ArrayList<PlaceModel> resultList;
    private JSONObject jsonArray_structured_formatting;
    private int textViewResourceId;
    private TextView tvName;
    private TextView tvTitleName;
    private Context context;
    private String searchByCountriesList;

    public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId, String searchByCountriesList) {
        super(context, textViewResourceId);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.searchByCountriesList = searchByCountriesList;
    }


    @Override
    public int getCount() {
        if (resultList != null)
            return resultList.size();
        else
            return 0;
    }

    @Override
    public PlaceModel getItem(int index) {
        return resultList.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_google_places_autocomplete_text, parent, false);
        }

        tvName = convertView.findViewById(R.id.locationTextView);
        tvTitleName = convertView.findViewById(R.id.locationTextViewTitle);

        tvName.setText(getItem(position).getDescription());
        tvTitleName.setText(getItem(position).getTitleAddress());

        return convertView;
    }

    public ArrayList<PlaceModel> autocomplete(String input) {
        ArrayList<PlaceModel> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + MapConstant.API_KEY);
            if (searchByCountriesList == null) {
                sb.append("&components=country:sg|country:in");
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(searchByCountriesList);
                    countryArray = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        countryArray += "country:" + jsonArray.getString(i) + "|";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                countryArray = countryArray.substring(0, countryArray.length() - 1);
                sb.append("&components=").append(countryArray);
            }
            sb.append("&input=").append(URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(CLASS_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(CLASS_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray(PREDICTIONS);

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());


            for (int i = 0; i < predsJsonArray.length(); i++) {

                jsonArray_structured_formatting = new JSONObject(predsJsonArray.getJSONObject(i).getString(STRUCTURED_FORMATTING));

                resultList.add(new PlaceModel(jsonArray_structured_formatting.getString(SECONDARY_TEXT),
                        predsJsonArray.getJSONObject(i).getString(PLACE_ID),
                        jsonArray_structured_formatting.getString(MAIN_TEXT)));
            }
        } catch (JSONException e) {
            Log.e(CLASS_TAG, context.getString(R.string.cannot_process_json), e);
        }

        return resultList;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
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
    }
}
