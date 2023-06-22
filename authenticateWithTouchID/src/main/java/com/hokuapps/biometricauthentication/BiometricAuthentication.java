package com.hokuapps.biometricauthentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

@SuppressWarnings("ALL")
public class BiometricAuthentication extends AppCompatActivity {

    private BiometricManager biometricManager;
    private Executor executor;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    String nextButtonCallBacK;
    @SuppressWarnings("SpellCheckingInspection")
    String jsonObjec;
    private final int BIOMETRIC_AUT = 1991;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.biometric_authentication);

        biometricManager = BiometricManager.from(this);

        instanceOfBiometricPrompt();

        if(getIntent().getExtras()!= null)
        {
            nextButtonCallBacK = getIntent().getStringExtra("nextButtonCallback");
            jsonObjec = getIntent().getStringExtra("jasonObject");
        }


        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS){
            // TODO: show in-app settings, make authentication calls
            biometricPrompt.authenticate(getPromptInfo());
        } else {
            Intent intent =new Intent();
            intent.putExtra("nextButtonCallBack",nextButtonCallBacK);
            intent.putExtra("jasonObject",jsonObjec);
            intent.putExtra("Result", "1");
            setResult(BIOMETRIC_AUT,intent);
            finish();

        }
    }


    private BiometricPrompt instanceOfBiometricPrompt()

    {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Intent intent =new Intent();
                intent.putExtra("nextButtonCallBack",nextButtonCallBacK);
                intent.putExtra("jasonObject",jsonObjec);
                intent.putExtra("Result", "0");
                setResult(BIOMETRIC_AUT,intent);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent =new Intent();
                intent.putExtra("nextButtonCallBack",nextButtonCallBacK);
                intent.putExtra("jasonObject",jsonObjec);
                intent.putExtra("Result", "1");
                setResult(BIOMETRIC_AUT,intent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Intent intent =new Intent();
                intent.putExtra("nextButtonCallBack",nextButtonCallBacK);
                intent.putExtra("jasonObject",jsonObjec);
                intent.putExtra("Result", "0");
                setResult(BIOMETRIC_AUT,intent);
            }
        });
        return biometricPrompt;
    }


    private BiometricPrompt.PromptInfo getPromptInfo()  {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Application")
                .setSubtitle("Confirm your screen lock pattern, PIN or password")
                .setDeviceCredentialAllowed(true)
                .build();
        return promptInfo;
    }



}