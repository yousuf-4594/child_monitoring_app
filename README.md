# child_monitoring_app
Android app is designed to monitor keystrokes of younger children helpful in parental supervision

App runs as background service indefinitely on android >= 8.0

Records keystrokes against the Application package child was in

logs are in format:
(DATE|TYPE)[LOGs..]


When filesize greater than 3kbs it uploads the log file to firebase if internet connectivity avaliable

Everyday a new log document is generated

You can view the log history in cloud firestore
