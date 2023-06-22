package com.hokuapps.calendaroprations;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(PowerMockRunner.class)
@PrepareForTest({CalendarEventValidator.class})
class CalendarEventValidatorTest {

    @Mock
    CalendarEventValidator calendarEventValidator;

    @Mock
    Context context;

    @Mock
    JSONObject obj;

    public CalendarEventValidatorTest() {

    }


    @Test
    void validate() {

        obj = Mockito.mock(JSONObject.class);
        calendarEventValidator = Mockito.mock(CalendarEventValidator.class);

        try {
            obj.put("action","add");
            obj.put("endDate",new long[13]);
            obj.put("eventTitle","Title");
        } catch (JSONException e) {
            e.printStackTrace();
        }
       // String data = "\{"organizationID":"5ecfe9029cf5734f0bf69d10","userID":"0596a3d98ca1cbedf504d678","appID":"5ec901db15c2ba186cd37c7e","nextButtonCallback":"setloginNativeCallBack","action":"","colorCode":"#3c3c3c"}";
       long val = 92233720368L;
        String eventdata = "{\"action\":\" add\",\"eventTitle\":\"Good City\",\"endDate\":"+val+"}";


        Gson gson= new Gson();
        CalenderEventModel eventModel = gson.fromJson(eventdata,CalenderEventModel.class);
        calendarEventValidator.validate(eventModel);

    }
}