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

    /**
     * Open the given pdf file in suitable app
     * @param context- context of activity
     * @param filePath - path of file
     * @param type- type of file
     */
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

    /**
     * get root directory path
     * @return
     */
    public static String getRootDirPath() {
        return getRootDir() + File.separator + new ShareAppData().applicationName;
    }



    /**
     * Get root directory
     * @return
     */
    public static File getRootDir() {
        return Environment.getExternalStorageDirectory();
    }


    /**
     * Show toast message to user
     * @param context
     * @param msg
     */
    public static void showMessage(Context context, String msg) {
        if (context != null && !TextUtils.isEmpty(msg))
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    /**
     * Save to calender
     * @param context - context of thr activity
     * @param calEvtString json object string
     */
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

    /**
     * Get the boolean value from json object
     * @param obj
     * @param fieldName
     * @return
     */
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

    /**
     * Get the string value from json object
     * @param obj-JsonObject
     * @param fieldName-key Value
     * @return
     */
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

    /**
     * Get the current date and time in milli seconds
     * @return
     */
    public static long getCurrentDateTimeInMS() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime().getTime();
    }


    /**
     * Get the required value from json object
     * @param obj - jsonObject
     * @param fieldName- key value
     * @return
     */
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


    /**
     * Get the status of the event
     * @param statusStr
     * @return
     */
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

    /**
     * Share the provided string via message app
     * @param context
     * @param sharedText
     */
    public static void shareTextViaMessageApp(Context context, String sharedText) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("sms_body", sharedText);
        context.startActivity(smsIntent);
    }

    /**
     * Share text message
     * @param context
     * @param messageText
     * @param hasSendMultiple
     */
    public static void shareTextMessage(Context context, String messageText, boolean hasSendMultiple) {
        shareTextMessage(context, messageText, hasSendMultiple, TYPE_PLAIN_TEXT);
    }

    /**
     * Share text message to multiple if it has
     * @param context
     * @param messageText
     * @param hasSendMultiple
     * @param type
     */
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

    /**
     * Share text via mail
     * @param context
     * @param messageText
     */
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

    /**
     * Share text via mail
     * @param context - context to activity
     * @param email- msg to share
     * @param message
     * @param data
     */
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

    /**
     * Open dial pad with provided number
     * @param context
     * @param mobileNo - mobile no to open in dialer
     */
    public static void openDialer(Context context, String mobileNo) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + mobileNo));
        context.startActivity(dialIntent);
    }


    /**
     * Get Message intent
     * @param context
     * @param messageText
     * @param hasSendMultiple
     * @return
     */
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

    /**
     * create intent to send multiple content
     * @param context
     * @return
     */
    public static Intent createSendMultipleIntent(Context context) {
        return new Intent(Intent.ACTION_SEND_MULTIPLE);
    }

    /**
     * create intent to send single content
     * @param context
     * @return
     */
    public static Intent createSendIntent(Context context) {
        return new Intent(Intent.ACTION_SEND);
    }

    /**
     * open browser with given url
     * @param context
     * @param url-open url on default browser
     */
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

    /**
     * convert UTC date format to local date format
     * @param dateString
     * @return
     */
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

}
