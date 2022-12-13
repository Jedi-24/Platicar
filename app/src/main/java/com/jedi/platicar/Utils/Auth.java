package com.jedi.platicar.Utils;

import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jedi.platicar.MainActivity;

public class Auth {
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final String TAG = "Auth";

    public interface AuthCallBack {
        void onCall(ActivityResult result);
    }

    public static AuthCallBack authCallBack = null;

    public static void oneTapGoogleSignIn(AppCompatActivity activity, SignInClient oneTapClient, BeginSignInRequest signInRequest,
                                          ActivityResultLauncher<IntentSenderRequest> launcher) {
        Log.d(TAG, "oneTapGoogleSignIn: starting");
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(signInResult ->
                performAuthentication(activity, oneTapClient, signInResult, launcher)).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getLocalizedMessage()));
    }

    private static void performAuthentication(AppCompatActivity activity, SignInClient oneTapClient, BeginSignInResult signInResult,
                                              ActivityResultLauncher<IntentSenderRequest> launcher) {
        authCallBack = result -> {
            if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                try {
                    SignInCredential intentCred = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String tokenID = intentCred.getGoogleIdToken();
                    AuthCredential credential = GoogleAuthProvider.getCredential(tokenID, null);

                    FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String currUserId = mAuth.getCurrentUser().getUid();
                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task12 -> {
                                        Log.d(TAG, "performAuthentication: "+ task12.getResult());
                                        FirebaseDatabase.getInstance().getReference().child("tokens")
                                                .child(currUserId).setValue(task12.getResult())
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        activity.startActivity(new Intent(activity, MainActivity.class));
                                                        activity.finish();
                                                    }
                                                });

                                    });
                                } else {
                                    Log.d(TAG, "onComplete: failure");
                                }
                            });
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        };
        launcher.launch(new IntentSenderRequest.Builder(signInResult.getPendingIntent().getIntentSender()).build());
    }
}
