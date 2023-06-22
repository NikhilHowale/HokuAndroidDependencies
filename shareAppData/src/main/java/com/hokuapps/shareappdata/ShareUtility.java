package com.hokuapps.shareappdata;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ShareUtility {

    public static final String TYPE_EMAIL_MESSAGE = "message/rfc822";
    public static final String TYPE_PLAIN_TEXT = "text/plain";
    public static final String FOLDER_NAME_MEDIA = "Media";


    public static void showPdfFileInApp(Context context, String filePath, String type) {

        File file = new File(filePath);
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setData(uri);
                context.startActivity(intent);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                intent = Intent.createChooser(intent, "Open File");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException ex) {
            ShareUtility.showMessage(context, "No Application Available to View PDF");
            ex.printStackTrace();
        }


    }


    public static String getRootDirPath() {
        return getRootDir() + File.separator + new ShareAppData().applicationName;
    }

    public static File getMediaDirPath() {
        String path = getRootDir() + File.separator + new ShareAppData().applicationName;
        return new File(path, ShareUtility.FOLDER_NAME_MEDIA);
    }

    @SuppressLint("QueryPermissionsNeeded")
    public static boolean isPackageExists(Context context, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }



    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            return mediaStorageDir.mkdirs();
        }
        return true;
    }



    public static File getRootDir() {
        return Environment.getExternalStorageDirectory();
    }


    public static void launchApplication(Context context, String packageNameTobeLaunch) {
        try {
            if (!TextUtils.isEmpty(packageNameTobeLaunch)) {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageNameTobeLaunch);
                launchIntent.putExtra("pageName", "index.html");
                context.startActivity(launchIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showMessage(Context context, String msg) {
        if (context != null && !TextUtils.isEmpty(msg))
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    public static void saveToCalendar(Context context, String calEvtString) {
        if (context == null) return;

        try {

            JSONObject calEvtJsonObj = new JSONObject(calEvtString);

            Object dtstartObj = getJsonObjectValue(calEvtJsonObj, "dtstart");
            Object dtendObj = getJsonObjectValue(calEvtJsonObj, "dtend");

            String title = getStringObjectValue(calEvtJsonObj, "summary");
            String description = getStringObjectValue(calEvtJsonObj, "description");
            String status = getStringObjectValue(calEvtJsonObj, "status");

            boolean isEventAllDay = getJsonObjectBooleanValue(calEvtJsonObj, "isEvtAllDay");

            //get start and end time
            long startTime = 0;
            long endTime = 0;

            if (dtstartObj instanceof String) {
                startTime = convertUTCDateToLocalDate(getStringObjectValue(calEvtJsonObj, "dtstart"));
            } else if (dtstartObj != null && dtendObj instanceof JSONObject) {
                startTime = convertUTCDateToLocalDate(getStringObjectValue((JSONObject) dtstartObj, "_value"));
            }

            if (dtendObj instanceof String) {
                endTime = convertUTCDateToLocalDate(getStringObjectValue(calEvtJsonObj, "dtend"));
            } else if (dtendObj instanceof JSONObject) {
                endTime = convertUTCDateToLocalDate(getStringObjectValue((JSONObject) dtendObj, "_value"));
            }

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime == 0 ? getCurrentDateTimeInMS() : startTime);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime == 0 ? getCurrentDateTimeInMS() : endTime);

            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, isEventAllDay);
            if (status != null) {
                intent.putExtra(CalendarContract.Events.STATUS, getEventStatus(status));
            }
            intent.putExtra(CalendarContract.Events.TITLE, TextUtils.isEmpty(title) ? "" : title);
            intent.putExtra(CalendarContract.Events.DESCRIPTION, TextUtils.isEmpty(description) ? "" : description);
            intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");
            context.startActivity(intent);

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean getJsonObjectBooleanValue(JSONObject obj, String fieldName) {
        if (obj == null) return false;

        try {

            if (obj.has(fieldName)) {
                return obj.getBoolean(fieldName);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public static String getStringObjectValue(JSONObject obj, String fieldName) {
        try {
            if (obj == null) return "";

            if (obj.has(fieldName)) {
                Object o = obj.get(fieldName);
                return o.toString();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static long getCurrentDateTimeInMS() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime().getTime();
    }


    public static Object getJsonObjectValue(JSONObject obj, String fieldName) {
        try {
            if (obj == null) return null;
            if (obj.has(fieldName)) {
                return obj.get(fieldName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    private static int getEventStatus(String statusStr) {

        if (statusStr.equalsIgnoreCase("CONFIRMED")) {
            return CalendarContract.Events.STATUS_CONFIRMED;
        } else if (statusStr.equalsIgnoreCase("TENTATIVE")) {
            return CalendarContract.Events.STATUS_TENTATIVE;
        } else if (statusStr.equalsIgnoreCase("CANCELED")) {
            return CalendarContract.Events.STATUS_CANCELED;
        }

        return -1;
    }


    public static void shareTextViaMessageApp(Context context, String sharedText) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("sms_body", sharedText);
        context.startActivity(smsIntent);
    }


    public static void shareTextMessage(Context context, String messageText, boolean hasSendMultiple) {
        shareTextMessage(context, messageText, hasSendMultiple, TYPE_PLAIN_TEXT);
    }


    @SuppressLint("QueryPermissionsNeeded")
    public static void shareTextMessage(Context context, String messageText, boolean hasSendMultiple, String type) {
        Intent shareIntent = createSendIntent(context);
        shareIntent.setType(type);

        ArrayList<Intent> intentList = getMessageIntent(context, messageText, hasSendMultiple);


        Intent chooserIntent = Intent.createChooser(
                intentList.remove(0), "Share text to..");

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));

        // Verify the intent will resolve to at least one activity
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooserIntent);
        }
    }


    @SuppressLint("IntentReset")
    public static void shareTextViaMail(Context context, String messageText) {
        Intent mail = new Intent(Intent.ACTION_SENDTO);
        mail.setType(TYPE_EMAIL_MESSAGE);
        mail.setData(Uri.parse("mailto:"));
        mail.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        mail.putExtra(android.content.Intent.EXTRA_TEXT, messageText);
        try {
            Intent chooserIntent = Intent.createChooser(
                    mail, "Share text to..");
            context.startActivity(chooserIntent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void shareTextViaMail(Context context, String email, String message, String data) {
        Intent mail = new Intent(Intent.ACTION_SENDTO);
        mail.setType(TYPE_EMAIL_MESSAGE);
        mail.setData(Uri.parse("mailto:"));
        mail.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        mail.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        mail.putExtra(android.content.Intent.EXTRA_TEXT, " ");
        Intent chooserIntent = Intent.createChooser(
                mail, "Share text to..");
        context.startActivity(chooserIntent);
    }

    public static void openDialer(Context context, String mobileNo) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + mobileNo));
        context.startActivity(dialIntent);
    }


    public static ArrayList<Intent> getMessageIntent(Context context, String messageText, boolean hasSendMultiple) {
        ArrayList<Intent> plainTextIntent = new ArrayList<>();
        Intent shareIntent = hasSendMultiple ? createSendMultipleIntent(context) : createSendIntent(context);
        shareIntent.setType(TYPE_PLAIN_TEXT);

        PackageManager pm = context.getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> activities = pm.queryIntentActivities(shareIntent, 0);

        for (ResolveInfo resInfo : activities) {
            final String pkg = resInfo.activityInfo.packageName;
            final String appName = resInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
            Intent intent = new Intent();
            intent.setAction(hasSendMultiple ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND);
            intent.setPackage(pkg);
            intent.setType(TYPE_PLAIN_TEXT);

            if (pkg.contains(context.getPackageName()) || appName.equals(context.getApplicationInfo().name)) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(messageText).append("\n\n");
            sb.append("Sent via the " + "AppConstant.APP_TAG");

            intent.putExtra(Intent.EXTRA_TEXT, sb.toString());

            plainTextIntent.add(intent);
        }

        return plainTextIntent;
    }

    public static Intent createSendMultipleIntent(Context context) {
        return new Intent(Intent.ACTION_SEND_MULTIPLE);
    }


    public static Intent createSendIntent(Context context) {
        return new Intent(Intent.ACTION_SEND);
    }

    public static void openBrowserIntent(Context context, String url) {
        if (context == null) return;
        try {
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                url = "http://" + url;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception ex) {
            showMessage(context, "No Application Available to View in browser");
            ex.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static long convertUTCDateToLocalDate(String dateString) {
        if (TextUtils.isEmpty(dateString)) return 0;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        long value = 0;
        try {
            Date date = format.parse(dateString);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mmaa");
            dateFormatter.setTimeZone(TimeZone.getDefault());

            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }
            value = calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @SuppressLint("SimpleDateFormat")
    public static long convertUTCDateToLocalDate(String dateString, String pattern) {
        if (TextUtils.isEmpty(dateString)) return 0;
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        long value = 0;
        try {
            Date date = format.parse(dateString);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mmaa");
            dateFormatter.setTimeZone(TimeZone.getDefault());

            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }
            value = calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return value;
    }

    @SuppressLint("SimpleDateFormat")
    public static Date convertUTCDateToLocalDateTime(String dateString) {
        if (TextUtils.isEmpty(dateString) || dateString.equals("0")) return null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = format.parse(dateString);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            dateFormatter.setTimeZone(TimeZone.getDefault());
            String dt = null;
            if (date != null) {
                dt = dateFormatter.format(date);
            }
            return dateFormatter.parse(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


}
