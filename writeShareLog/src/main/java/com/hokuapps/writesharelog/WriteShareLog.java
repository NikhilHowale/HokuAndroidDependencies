package com.hokuapps.writesharelog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteShareLog {
    private static WriteShareLog instance;

    private String FOLDER_NAME = "Logcat";
    private String FILE_NAME = "logcat.txt";

    public static WriteShareLog getInstance(){
        if(instance == null){
            instance = new WriteShareLog();
        }
        return instance;
    }

    public void writeLogs(Context context, String data) {
        try {
            JSONObject targetDataJsonObj = new JSONObject(data);
            String logString = Utility.getStringObjectValue(targetDataJsonObj, "log");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new
                    File(context.getFilesDir() + File.separator + FOLDER_NAME + File.separator + FILE_NAME)));
            bufferedWriter.write(logString);
            bufferedWriter.close();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void shareLogs(Context context, String data){
        try {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            File appDirectory = new File(context.getFilesDir(), FOLDER_NAME);
            String filePath = appDirectory.getPath();
            File file = new File(filePath, FILE_NAME);
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            sendIntent.setType("*/*");
            context.startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
