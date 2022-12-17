package com.jedi.platicar.Utils;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NotificationDispatcher {

    private static final String TAG = "NotificationDispatcher";

    private NotificationDispatcher() {
    }

    private static final String[] SCOPES = {"https://www.googleapis.com/auth/firebase.messaging"};
    private static final String apiHitEndPoint = "https://fcm.googleapis.com/v1/projects/platicar-4ad1d/messages:send";
    private static String title = "";

    private static void sendNotification(Context ctx, String accessToken, String device_token, HashMap<String, String> textBody) {
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);

        JSONObject mainObj = new JSONObject();
        try {
            JSONObject messageObj = new JSONObject();
            messageObj.put("token", device_token);

            JSONObject notifObj = new JSONObject();
            notifObj.put("body", textBody.get("message"));

            if (textBody.get("from").matches("NEW CHAT REQUEST"))
                notifObj.put("title", textBody.get("from"));
            else {
                notifObj.put("title", title);
            }

            messageObj.put("notification", notifObj);
            mainObj.put("message", messageObj);

            Log.d(TAG, "sendNotification: JSON: " + mainObj.toString(4));

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, apiHitEndPoint, mainObj,
                    response -> Log.d(TAG, "sendNotification:" + response.toString()), error -> Log.d(TAG, error.toString())) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "Bearer " + accessToken);
                    return header;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void dispatchNotification(Context context, String device_token, HashMap<String, String> textBody) {
        getName();
        InputStream jsonFile = context.getResources().openRawResource(context.getApplicationContext().getResources()
                .getIdentifier("service_account", "raw", context.getPackageName()));
        new Thread(() -> {
            try {
                GoogleCredentials googleCredentials = GoogleCredentials
                        .fromStream(jsonFile)
                        .createScoped(Arrays.asList(SCOPES));

                String accessToken = googleCredentials.refreshAccessToken().getTokenValue();
                new Handler(context.getMainLooper()).post(() ->
                        sendNotification(context.getApplicationContext(), accessToken, device_token, textBody));
            } catch (IOException e) {
                Log.d(TAG, "In error statement");
                e.printStackTrace();
            }
        }).start();
    }

    static void getName() {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        title = (String) snapshot.child("Name").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}
