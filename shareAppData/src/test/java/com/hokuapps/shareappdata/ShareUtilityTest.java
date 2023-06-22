package com.hokuapps.shareappdata;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ShareUtility.class, TextUtils.class, Uri.class})
class ShareUtilityTest {

    @Mock
    ShareUtility shareUtility;

    @Mock
    Context context;

    @Mock
    JSONObject obj;

    @Mock
    Intent intent;

    @Mock
    private Uri mockUri;

    @Mock
    PackageManager pm;



    public ShareUtilityTest() {

    }


    @Before
    public void setUp() {
        try {
            whenNew(Intent.class).withAnyArguments().thenReturn(intent);
            PowerMockito.mockStatic(Uri.class);
            when(Uri.parse("mailto:")).thenReturn(mockUri);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void convertUTCDateToLocalDate_Failed() {

        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
        String sDate1="31/12/1998";
        shareUtility.convertUTCDateToLocalDate(sDate1);
    }

    @Test
    public void convertUTCDateToLocalDate_Passed() {

        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
        String sDate1="2011-11-02T02:50:12.208Z";
        shareUtility.convertUTCDateToLocalDate(sDate1);
    }

    @Test
    public void shareTextViaMail(){

        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
        shareUtility.shareTextViaMail(context,"Hiiiiii");

    }

    @Test
    public void openBrowserIntent(){

        shareUtility.openBrowserIntent(context,"Hiiiiii");

    }

    @Test
    public void getJsonObjectValue(){
        //obj=new JSONObject();
        try {
            obj.put("name","sonoo");
            obj.put("age",new Integer(27));
            obj.put("salary",new Double(600000));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        shareUtility.getJsonObjectValue(obj,"age");

    }

    @Test
    public void getJsonObjectBooleanValue(){

      //   obj=new JSONObject();
        try {
            obj.put("name","sonoo");
            obj.put("age",new Integer(27));
            obj.put("salary",true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        shareUtility.getJsonObjectBooleanValue(obj,"salary");

    }

    @Test
    public void launchApplication(){
        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
        shareUtility.launchApplication(context,"Hiiiiii");

    }
}