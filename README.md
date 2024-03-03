# child_monitoring_app
Android app is designed to monitor keystrokes of younger children helpful in parental supervision

App runs as background service indefinitely on android >= 8.0

Records keystrokes against the Application package child was in

logs are in format:
(DATE|TYPE)[LOGs..]


When filesize greater than 3kbs it uploads the log file to firebase if internet connectivity avaliable

Everyday a new log document is generated

You can view the log history in cloud firestore
![image](https://github.com/yousuf-4594/child_monitoring_app/assets/108923755/3861b6f8-0285-41cb-b35e-b828300f90c6)
