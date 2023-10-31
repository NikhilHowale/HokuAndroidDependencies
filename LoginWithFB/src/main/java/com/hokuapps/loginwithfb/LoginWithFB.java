package com.hokuapps.loginwithfb;

import static com.hokuapps.loginwithfb.constants.LoginConstants.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.hokuapps.loginwithfb.utility.Utility;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class LoginWithFB {

    @SuppressLint("StaticFieldLeak")
    private static LoginWithFB instance;
    private Activity mActivity;
    private WebView mWebView;
    private String facebookLoginCallbackFunction = null;
    private CallbackManager callbackManager;

    public static LoginWithFB getInstance(){
        if(instance == null){
            instance = new LoginWithFB();
        }
        return instance;
    }

    /**
     * This method initialize parameter
     * @param mActivity activity reference
     * @param mWebView web-view instance
     */
    public void init(Activity mActivity, WebView mWebView) {
        this.mActivity = mActivity;
        this.mWebView = mWebView;
        callbackManager = CallbackManager.Factory.create();
    }


    /**
     * This method call facebook login activity
     * @param googleLogin response data
     */
    public void loginWithFB(String googleLogin){
        try {

            facebookLoginCallbackFunction = Utility.getStringObjectValue(new JSONObject(googleLogin), keyConstants.NEXT_BUTTON_CALLBACK);
            mActivity.runOnUiThread(() -> {

                LoginManager.getInstance();
                LoginManager.getInstance().logOut();

                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(final LoginResult loginResult) {
//                              Graph request to get user profile information.
                                Bundle params = new Bundle();
                                params.putString(keyConstants.FIELDS, "name,id,email,picture.type(large)");
                                requestUserProfile(loginResult);
                            }

                            @Override
                            public void onCancel() {
                                JSONObject jsonObjCancel = new JSONObject();
                                try {
                                    jsonObjCancel.put(keyConstants.IS_CANCEL, true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Utility.callJavaScriptFunction(mActivity, mWebView, facebookLoginCallbackFunction, jsonObjCancel);
                            }

                            @Override
                            public void onError(@NonNull FacebookException exception) {
                                Utility.showMessage(mActivity, exception.getMessage());
                                // App code
                                JSONObject jsonObjError = new JSONObject();
                                try {
                                    jsonObjError.put(keyConstants.ERROR_CODE, 500);
                                    jsonObjError.put(keyConstants.ERROR_MESSAGE, exception.getMessage());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Utility.callJavaScriptFunction(mActivity, mWebView, facebookLoginCallbackFunction, jsonObjError);
                            }
                        });

                LoginManager.getInstance().logInWithReadPermissions(mActivity, Arrays.asList(keyConstants.EMAIL, "public_profile"));

            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method handle activity result after facebook sign
     * @param requestCode The request code that's received by the Activity
     * @param resultCode The result code that's received by the Activity
     * @param intent The result data that's received by the Activity
     */
    public void handleFaceBookSignIn(int requestCode, int resultCode, Intent intent){
        callbackManager.onActivityResult(requestCode,resultCode,intent);
    }

    /**
     * Get user profile from the facebook response object.
     * @param loginResult The login result after user sign successfully
     */
    public void requestUserProfile(final LoginResult loginResult) {
        final String accessToken = loginResult.getAccessToken().getToken();

        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (object, response) -> {
            // Get facebook data from login

            if(object == null) return;

            JSONObject jsonObject = getFacebookData(object);
            try {
                if(jsonObject != null) {
                    String id = object.getString(keyConstants.ID);
                    jsonObject.put(keyConstants.FB_ID_TOKEN, id);
                    jsonObject.put(keyConstants.TOKEN, accessToken);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(facebookLoginCallbackFunction)) {
                if (jsonObject != null) {
                    Utility.callJavaScriptFunction(mActivity, mWebView, facebookLoginCallbackFunction, jsonObject);
                } else {
                    JSONObject jsonObjError = new JSONObject();
                    try {
                        jsonObjError.put(keyConstants.ERROR_CODE, 500);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Utility.callJavaScriptFunction(mActivity, mWebView, facebookLoginCallbackFunction, jsonObjError);
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString(keyConstants.FIELDS, "id, first_name, last_name, email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * Extract facebook data from jsonObject.
     * @param object JSONObject
     */
    private JSONObject getFacebookData(JSONObject object) {

        try {
            JSONObject jsonObject = new JSONObject();
            String id = object.getString(keyConstants.ID);

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                jsonObject.put(keyConstants.PHOTO_URL, profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            if (object.has(keyConstants.FIRST_NAME))
                jsonObject.put(keyConstants.NAME, object.getString(keyConstants.FIRST_NAME));
            if (object.has(keyConstants.LAST_NAME))
                jsonObject.put(keyConstants.LAST_NAME, object.getString(keyConstants.LAST_NAME));
            if (object.has(keyConstants.EMAIL))
                jsonObject.put(keyConstants.EMAIL, object.getString(keyConstants.EMAIL));
            if (object.has(keyConstants.GENDER))
                jsonObject.put(keyConstants.GENDER, object.getString(keyConstants.GENDER));
            if (object.has(keyConstants.BIRTHDAY))
                jsonObject.put(keyConstants.BIRTHDAY, object.getString(keyConstants.BIRTHDAY));
            if (object.has(keyConstants.LOCATION))
                jsonObject.put(keyConstants.LOCATION, object.getJSONObject(keyConstants.LOCATION).getString(keyConstants.NAME));

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
