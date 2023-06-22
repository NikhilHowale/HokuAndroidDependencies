package com.hokuapps.calendaroprations;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class CalendarEventManager {

    private static final String TAG = CalendarEventManager.class.getSimpleName();
    private final Activity activity;

    public EventResult eventResult = new EventResult();


    private CalendarEventManager(Activity activity) {
        this.activity = activity;
    }

    public static CalendarEventManager Builder(Activity activity) {
        return new CalendarEventManager(activity);
    }


    public EventResult performEventCRUDOperations(CalenderEventModel eventModel)
    {

        if ( eventModel != null && eventModel.getAction()!=null) {

            switch (eventModel.getAction()) {
                case "add":
                    // code block
                    if (!isEventAlreadyExist(eventModel.getEventTitle(), eventModel.getStartDate(), eventModel.getEndDate())) {
                        eventResult = addCalenderEvent(eventModel);
                    } else {
                        eventResult.setStatus("1");
                        eventResult.setMessage("Event Already Exists");
                    }

                    break;
                case "update":
                    eventResult = updateEvent(eventModel);
                    break;
                case "read":
                case "delete":
                    // code block
                    break;
                default:
                    // code block
            }
        }



        return eventResult;

    }

    private EventResult addCalenderEvent(CalenderEventModel eventModel) {
        EventResult result = new EventResult();
        try {
            ContentResolver cr = activity.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(CalendarContract.Events.DTSTART, eventModel.getStartDate());
            values.put(CalendarContract.Events.DTEND, eventModel.getEndDate());
            values.put(CalendarContract.Events.TITLE, eventModel.getEventTitle());
            values.put(CalendarContract.Events.DESCRIPTION, eventModel.getNotes());

            // Default calendar
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            // Insert event to calendar
            Uri eventUri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            long eventID = Long.parseLong(eventUri.getLastPathSegment());

            //Check for event Added successfully
            if(eventID > 0)
            {
                result.setStatus("0");
                result.setMessage("Event Added Successfully");

                return result;
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            result.setStatus("1");
            result.setMessage(ex.getMessage());
            return result;
        }

        return result;

    }


    @SuppressLint("Recycle")
    private boolean isEventAlreadyExist(String eventTitle, long startDate, long endDate) {
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, startDate);
        ContentUris.appendId(eventsUriBuilder, endDate);
        Uri eventsUri = eventsUriBuilder.build();

        String[] column = {CalendarContract.Instances.EVENT_ID, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.TITLE};
        Cursor cursor;
        cursor = activity.getContentResolver().query(eventsUri, column, CalendarContract.Events.CALENDAR_ID+"="+1, null, CalendarContract.Instances.DTSTART + " ASC");

        return cursor.getCount() > 0;
    }

    @SuppressLint("Recycle")
    private Uri getUpdateUri(String eventTitle, long startDate, long endDate)
    {
        Uri updateUri = null;
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, startDate);
        ContentUris.appendId(eventsUriBuilder, endDate);
        Uri eventsUri = eventsUriBuilder.build();

        String[] column = {CalendarContract.Instances.EVENT_ID, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.TITLE};
        Cursor cursor;
        cursor = activity.getContentResolver().query(eventsUri, column, CalendarContract.Events.CALENDAR_ID+"="+1, null, CalendarContract.Instances.DTSTART + " ASC");



        // Submit the query

        if (cursor.moveToFirst()) {
            do {
                updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,cursor.getLong(0));
            } while (cursor.moveToNext());
        }

        return updateUri;
    }
    private  EventResult updateEvent(CalenderEventModel calenderEventModel) {
        EventResult result = new EventResult();
        ContentResolver cr = activity.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, calenderEventModel.getUpdatedStartDate());
        values.put(CalendarContract.Events.DTEND, calenderEventModel.getUpdatedEndDate());
        values.put(CalendarContract.Events.DESCRIPTION, calenderEventModel.getNotes());
        Uri updateUri = getUpdateUri(calenderEventModel.getEventTitle(),calenderEventModel.getStartDate(),calenderEventModel.getEndDate());
        if(updateUri!=null) {
            int rows = activity.getContentResolver().update(updateUri, values, null, null);
            result.setStatus("0");
            result.setMessage("Event Updated Successfully");
        }else{
            result.setStatus("1");
            result.setMessage("Event Update Failed");
        }


        return result;

    }

    private void deleteEvent(long eventID) {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        int rows = activity.getContentResolver().delete(deleteUri, null, null);
        Log.i("Calendar", "Rows deleted: " + rows);
    }

    public void readEvents( ) {
        final String[] INSTANCE_PROJECTION = new String[]{
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.TITLE,          // 2
                CalendarContract.Instances.ORGANIZER
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_BEGIN_INDEX = 1;
        final int PROJECTION_TITLE_INDEX = 2;
        final int PROJECTION_ORGANIZER_INDEX = 3;

        // Specify the date range you want to search for recurring event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2017, 9, 23, 8, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2018, 1, 24, 8, 0);
        long endMillis = endTime.getTimeInMillis();


        // The ID of the recurring event whose instances you are searching for in the Instances table
        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[]{"207"};

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        @SuppressLint("Recycle") Cursor cur = activity.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, null, null, null);


        ArrayList<String> events = new ArrayList<>();
        while (cur.moveToNext()) {

            // Get the field values
            long eventID = cur.getLong(PROJECTION_ID_INDEX);
            long beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            String title = cur.getString(PROJECTION_TITLE_INDEX);
            String organizer = cur.getString(PROJECTION_ORGANIZER_INDEX);

            // Do something with the values.
            Log.i("Calendar", "Event:  " + title);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            Log.i("Calendar", "Date: " + formatter.format(calendar.getTime()));

            events.add(String.format("Event ID: %d\nEvent: %s\nOrganizer: %s\nDate: %s", eventID, title, organizer, formatter.format(calendar.getTime())));
        }
    }



}
