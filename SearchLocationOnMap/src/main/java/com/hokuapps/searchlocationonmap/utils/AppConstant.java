package com.hokuapps.searchlocationonmap.utils;

public class AppConstant {



    public interface  google_LatLongFromPlaceId{
        String BASE_PATH_lATLONGFROMPLACEID = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
    }
    public interface googlePlace {

        // google Map places Near by you..
        String ID = "id";
        String PLACE_RESPONSE_RESULT = "result";
        String PLACE_RESULT = "results";
        String PLACE_OK = "OK";
        String PLACE_ID = "place_id";
        String PLACE_NAME = "name"; // this is Name of the location name on google map.
        String PLACE_VICINITY = "vicinity";
        String PLACE_GEOMETRY = "geometry";
        String PLACE_LOCATION = "location";
        String PLACE_REFERENCE = "reference";
        String PLACE_ICON = "icon"; // marker of the location for the google map
        String PLACE_LAT = "lat"; // latitute of the location for google map
        String PLACE_LNG = "lng"; // longitute of the location for google map
    }

    public interface IntentParam {
        String IS_RESULT_CANCEL = "is_result_cancel";

        String MAP_LOCATION_MODEL = "mapLocationModel";
        String EXTRA_MAP_RESULT_CALLBACK = "extra_result_callback";
    }

    public interface JSONParameter {
        String BACK_CALLBACK_FUNCTION = "backCallbackFunction";
        String NEXT_CALLBACK_FUNCTION = "nextButtonCallback";
        String ADDRESS = "address";
        String LATITUDE = "lat";
        String LONGITUDE = "long";
    }
}
