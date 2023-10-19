package com.hokuapps.Loadnativeqrcodescannerupload.utils;

public class BarcodeConstant {



    /**
     * Can not create instance of contant class
     */
    private BarcodeConstant() {
    }

    public interface RequestCode {
        // intent request code to handle updating play services if needed.
        int RC_HANDLE_GMS = 9001;
        // permission request codes need to be < 256
        int RC_HANDLE_CAMERA_PERM = 2;
        int RC_BARCODE_CAPTURE = 9001;
    }

    public interface IntentExtras {
        // constants used to pass extra data in the intent
        String AUTO_FOCUS = "AUTO_FOCUS";
        String USE_FLASH = "USE_FLASH";
        String BARCODE_OBJECT = "Barcode";
        String MULTIPLE_BARCODE_OBJECT = "MultipleBarcode";
        String REQUEST_JSON_OBJ_STR = "requestJsonStr";
        String REQUEST_CALL_BACK_OBJ = "requestCallBackObj";
    }
}
