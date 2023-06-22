package com.hokuapps.calendaroprations;

import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;

public class CalendarEventValidator {

    CalendarEventValidator()
    {

    }

    public  ArrayList<String> validate(CalenderEventModel eventModel)
    {
        ArrayList<String> missingKeys = new ArrayList();

        if ( eventModel != null && eventModel.getAction()!= null) {
            switch (eventModel.getAction()) {
                case "add":
                    //for add action: eventTitle,startDate is mandetory
                    missingKeys = validateAddAction(eventModel);
                    break;
                case "update":
                    // code block
                    break;
                case "delete":
                    // code block
                    break;
                default:
                    //Invalid Action
            }
        }
        else {
            //missing action , valid actions (Add,update,Delete)
        }
        return missingKeys;
    }


    private  ArrayList<String> validateAddAction(CalenderEventModel eventModel)
    {
        ArrayList<String> missingKeys = new ArrayList();
        if(eventModel.eventTitle == null || eventModel.eventTitle.isEmpty())
            missingKeys.add("eventTitle parameter is missing");
        if(String.valueOf(eventModel.startDate) == null || eventModel.startDate == 0)
            missingKeys.add("startDate parameter is missing");

        return missingKeys;
    }

}
