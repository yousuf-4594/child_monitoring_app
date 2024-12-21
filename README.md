# child_monitoring_app
Android app is designed to monitor keystrokes of younger children helpful in parental supervision

App runs as background service indefinitely on android >= 8.0

Records keystrokes against the Application package child was in

logs are in format:
(DATE|TYPE) LOGs..


When filesize greater than 3kbs it uploads the log file to firebase if internet connectivity avaliable

Everyday a new log document is generated

You can view the log history in cloud firestore

![image]![Screenshot 2024-12-21 154513](https://github.com/user-attachments/assets/f1d98161-a167-489b-af04-26f3ebc47809)
