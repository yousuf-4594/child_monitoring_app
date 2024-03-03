package com.example.batterySaver;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class firebase_database {

    private FirebaseFirestore db;
    private CollectionReference recordsCollection;


    public static void addFieldToAnalyticsCollection(String log, String packageName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("analytics").document(currentDate);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        updateField(docRef, log, packageName);
                    } else {
                        createDocumentWithTimestampField(docRef, log, packageName);
                    }
                } else {
                    Log.d("battery manager", "get failed with ", task.getException());
                }
            }
        });
    }

    private static void createDocumentWithTimestampField(DocumentReference docRef, String log, String packageName) {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = dateFormat.format(new Date(currentTimeMillis));

        Map<String, Object> newData = new HashMap<>();
        Map<String, Object> timestampMap = new HashMap<>();
        timestampMap.put("log", log);
        timestampMap.put("package_name", packageName);
        newData.put(formattedTimestamp, timestampMap);
        docRef.set(newData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "Document created with field: " + formattedTimestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error creating document: " + e.getMessage());
                    }
                });
    }

    private static void updateField(DocumentReference docRef, String log, String packageName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        Map<String, Object> newData = new HashMap<>();
        Map<String, Object> timestampMap = new HashMap<>();
        timestampMap.put("log", log);
        timestampMap.put("package_name", packageName);
        newData.put(timestamp, timestampMap);
        docRef.update(newData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "Field '" + timestamp + "' added to document");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error updating field: " + e.getMessage());
                    }
                });
    }




    public static void addFieldToMonitoringCollection(String log) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("monitoring").document(currentDate);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        updateFieldMonitoring(docRef, log);
                    } else {
                        createDocumentWithTimestampFieldMonitoring(docRef, log);
                    }
                } else {
                    Log.d("battery manager", "get failed Monitoring with ", task.getException());
                }
            }
        });
    }

    private static void createDocumentWithTimestampFieldMonitoring(DocumentReference docRef, String log) {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = dateFormat.format(new Date(currentTimeMillis));

        Map<String, Object> newData = new HashMap<>();
        newData.put(formattedTimestamp, log);
        docRef.set(newData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "Document created with Monitoring field: " + formattedTimestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error creating Monitoring document: " + e.getMessage());
                    }
                });
    }

    private static void updateFieldMonitoring(DocumentReference docRef, String log) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        Map<String, Object> newData = new HashMap<>();
        newData.put(timestamp, log);
        docRef.update(newData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("battery manager", "Monitoring '" + timestamp + "' added to document");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("battery manager", "Error updating Monitoring: " + e.getMessage());
                    }
                });
    }
}
