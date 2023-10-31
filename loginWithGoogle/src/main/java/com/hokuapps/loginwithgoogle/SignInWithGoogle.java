package com.hokuapps.loginwithgoogle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;
@SuppressLint("StaticFieldLeak")
public class SignInWithGoogle {
    private static final String TAG = "SignInWithGoogle";
    public static final int RC_SIGN_IN = 9122;


    private static GoogleSignInClient googleSignInClient;
    private static String googleLoginCallbackFunction = null;

    public static GoogleSilentSignResultListener googleSilentSignResultListener;


    /**
     * This method initialize google sign in client
     * @param activity activity
     */
    public static void initGoogleSignIn(AppCompatActivity activity) {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(Utility.getRequestIdToken(activity))
                    .build();

            // Initialize sign in client
            googleSignInClient = GoogleSignIn.getClient(activity, gso);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method call sign in intent with display google account
     * @param activity activity
     * @param response json response in string format
     */
    public static void googleSignIn(Activity activity, String response) {
        try {
            if (googleSignInClient == null) return;

            googleLoginCallbackFunction = Utility.getStringObjectValue(new JSONObject(response), "nextButtonCallback");

            Intent signInIntent = googleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * This method checks social media login type. according to type log out from the app
     * @param response jsonObject in string format
     */
    public static void socialMediaLogout(String response) {
        try {
            JSONObject object = new JSONObject(response);
            int socialMediaType = Utility.getJsonObjectIntValue(object, "socialMediaType");

            if (socialMediaType == 1) {
                //google logout
                googleSignOut();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method signs out if the user login with google
     */
    public static void googleSignOut() {
        if (googleSignInClient == null) return;

        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e(TAG, "onComplete: google SignOut" );
            }
        });
    }

    /**
     *  If the user deletes an account, then an application should delete the information that your app obtained from the Google APIs.
     */
    public static void googleRevokeAccess() {
        if (googleSignInClient == null) return;
        googleSignInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e(TAG, "onComplete: google RevokeAccess" );
            }
        });
    }

    /**
     * This method handle result of google sign
     * @param activity activity
     * @param mWebView webView reference
     * @param data google sign in account data
     */
    public static void handleGoogleSignResult(Activity activity, WebView mWebView, Intent data){
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            JSONObject jsonObject = null;
            if(task.isSuccessful()) {
                parseGoogleSignInResult(task.getResult());
            }
            if (!TextUtils.isEmpty(googleLoginCallbackFunction)) {
                if (jsonObject != null) {
                    Utility.callJavaScriptFunction(activity, mWebView, googleLoginCallbackFunction, jsonObject);
                } else {
                    Log.d("GOOGLE_ERROR", "" + task.getException());
                    JSONObject jsonObjError = new JSONObject();
                    jsonObjError.put("errorCode", 500);
                    Utility.callJavaScriptFunction(activity, mWebView, googleLoginCallbackFunction, jsonObjError);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This methode retrieves data from googleSignInAccount
     * @param account GoogleSignInAccount
     * @return return jsonObject
     */
    private static JSONObject parseGoogleSignInResult(GoogleSignInAccount account) {

        JSONObject jsonObject = new JSONObject();
        try {
                jsonObject.put("email", account.getEmail());
                jsonObject.put("photoUrl", account.getPhotoUrl());
                jsonObject.put("name", account.getDisplayName());
                jsonObject.put("googleIdToken", account.getIdToken());
                jsonObject.put("idToken", account.getIdToken());
                jsonObject.put("userId", account.getId());

                return jsonObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * This method google sign without display any user interface
     * @param googleSilentSignResultListener callback
     */
    public static void googleSilentSignIn(final GoogleSilentSignResultListener googleSilentSignResultListener) {
        SignInWithGoogle.googleSilentSignResultListener = googleSilentSignResultListener;
        Task<GoogleSignInAccount> pendingResult =   googleSignInClient.silentSignIn();
        if (pendingResult.isComplete()) {

            // There's immediate result available.
            if (googleSilentSignResultListener != null && pendingResult.isSuccessful()) {
                googleSilentSignResultListener.silentSignInResult(parseGoogleSignInResult(pendingResult.getResult()));
            }
        } else {
            System.out.println("GOOGLE SIGNING IN ON ASYNC:");
            pendingResult.addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if(task.isSuccessful()){
                        if (googleSilentSignResultListener != null) {
                            googleSilentSignResultListener.silentSignInResult(parseGoogleSignInResult(task.getResult()));
                        }
                    }
                }
            });
        }

    }

    public interface GoogleSilentSignResultListener {
        void silentSignInResult(JSONObject jsonObjectResult);

    }
}
