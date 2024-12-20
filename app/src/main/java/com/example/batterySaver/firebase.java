package com.example.batterySaver;

import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class firebase {
    private FirebaseFirestore db;
    private CollectionReference recordsCollection;

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char[] arr = str.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);
        return new String(arr);
    }
    public static void uploadNotificationLogToFirebase(String log) {
        String deviceName = getDeviceName().replace(" ", "_").toLowerCase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        String timestamp = String.valueOf(System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(deviceName)
                .document("notifications")
                .collection(currentDate)
                .document(timestamp);

        Map<String, Object> data = new HashMap<>();
        data.put("log", log);
        data.put("timestamp", timestamp);

        docRef.set(data)
                .addOnSuccessListener(aVoid -> Log.d("firebase", "Notification log successfully uploaded."))
                .addOnFailureListener(e -> Log.e("firebase", "Error uploading notification log: " + e.getMessage()));
    }

    public static void updateAliveStatus() {
        String deviceName = getDeviceName().replace(" ", "_").toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(deviceName)
                .document("alive");

        Map<String, Object> data = new HashMap<>();
        data.put("last_seen", timestamp);
        data.put("device_name", deviceName);

        docRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "Alive status updated at: " + timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error updating alive status: " + e.getMessage());
                    }
                });}


    public static void uploadSystemLogsToFirebase(String log) {
        String deviceName = getDeviceName().replace(" ", "_").toLowerCase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        String timestamp = String.valueOf(System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(deviceName)
                .document("system_logs")
                .collection(currentDate)
                .document(timestamp);

        Map<String, Object> data = new HashMap<>();
        data.put("log", log);
        data.put("timestamp", timestamp);

        docRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "System logs data added for timestamp: " + timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error adding system logs data: " + e.getMessage());
                    }
                });
    }

    public static void addFieldToAnalyticsCollection(String log, String packageName) {
        String deviceName = getDeviceName().replace(" ", "_").toLowerCase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        String timestamp = String.valueOf(System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(deviceName)
                .document("analytics")
                .collection(currentDate)
                .document(timestamp);

        Map<String, Object> data = new HashMap<>();
        data.put("log", log);
        data.put("package_name", packageName);

        docRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "Analytics data added for timestamp: " + timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error adding analytics data: " + e.getMessage());
                    }
                });
    }

    public static void addFieldToMonitoringCollection(String log) {
        String deviceName = getDeviceName().replace(" ", "_").toLowerCase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        String timestamp = String.valueOf(System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(deviceName)
                .document("monitoring")
                .collection(currentDate)
                .document(timestamp);

        Map<String, Object> data = new HashMap<>();
        data.put("log", log);

        docRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "Monitoring data added for timestamp: " + timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error adding monitoring data: " + e.getMessage());
                    }
                });
    }
}