package com.hokuapps.loadmapviewbyconfig.synchronizer;

import android.os.AsyncTask;
import android.util.Log;


import com.hokuapps.loadmapviewbyconfig.utility.Utility;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapAsyncTask extends AsyncTask<String, Void, String> {

    private OnMapAsyncListener onMapAsyncListener;

    public MapAsyncTask() {

    }

    public void setOnMapAsyncListener(OnMapAsyncListener onMapAsyncListener) {
        this.onMapAsyncListener = onMapAsyncListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(onMapAsyncListener != null) {
            onMapAsyncListener.onMapAsyncTaskBefore();
        }
    }

    @Override
    protected String doInBackground(String... url) {

        String data = "";
        try {
            data = downloadUrl(url[0]);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        JSONObject object = Utility.convertStringToJson(result);

        if(onMapAsyncListener != null) {
            onMapAsyncListener.onMapAsyncTaskResult(object);
        }
    }


    /**
     *
     * @param strUrl
     * @return
     * @throws IOException
     */
    private String downloadUrl(String strUrl) throws IOException {

        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /**
     *
     */
    public  interface OnMapAsyncListener {
        void onMapAsyncTaskResult(JSONObject jsonObject);
        void onMapAsyncTaskBefore();
    }
}
