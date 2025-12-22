# TOTO CINEMA - App booking cinema ticket
*A cross-platform cinema-ticket booking system*

TOTO CINEMA lets moviegoers browse showtimes, choose seats and pay securely from an Android app while cinema staff manage films, schedules and sales via an admin portal.  
The solution is split into a **Python + Flask REST API** (with MySQL) and a **native Android (Java)** client, so you can run the server anywhere and point multiple apps to it.

---
## ✨ Key features
| User-facing | Admin-facing |
|-------------|--------------|
| 🔍 Search & filter movies by genre, date and cinema | 🎞️ CRUD films, showtimes & halls |
| 🪑 Real-time seat selection & hold logic | 🎫 View, confirm or cancel bookings |
| 💳 Booking & in-app payment flow | 📊 Daily revenue dashboard |
| 📜 Order history & e-tickets | 👤 Role-based accounts (admin / user) |

---
📋 Requirements
Before starting, make sure you have the following installed on your system:

## Java Development Kit (JDK)

Required Version: JDK 17 or JDK 21

Download: Oracle JDK or OpenJDK

## Python

Required Version: Python 3.12 or Python 3.13

Download: Python Downloads

## Ngrok

Install ngrok (available on microsoft store or chrome)

🔍 Verify Installations
Check your installed versions:

```bash
java --version
python --version
```
## 📦 Project Installation & Setup
1️⃣ Clone the Project

```bash
git clone https://github.com/Hieu200405/SE114_Q11_QLRCP
```



🖥️ Backend Setup (Python)
➡️ Create and Activate Virtual Environment

```bash
python -m venv venv
# Windows
venv\Scripts\activate
# macOS/Linux
source venv/bin/activate
```

➡️ Install Dependencies
```bash
cd backend_python
pip install -r requirements.txt
```

➡️ Configure Environment Variables
Edit the file ./backend_python/.env and update the following values:
```bash
KEY=""
DB_HOST=""
DB_PORT=""
DB_USER=""
DB_PASSWORD=""
DB_NAME=""
#PayOS Configuration
PAYOS_CLIENT_ID=""
PAYOS_API_KEY=""
PAYOS_CHECKSUM_KEY=""
```

📌 Replace {KEY}, {DB_HOST}, {DB_PORT}, {DB_USER}, {DB_PASSWORD}, {DB_NAME}, {PAYOS_CLIENT_ID}, {PAYOS_API_KEY}, {PAYOS_CHECKSUM_KEY} with your PayOS Configuration.
➡️ Database online
Use the database created online


➡️ Run Backend Server
Run ngrok
```bash
ngrok http 5000
```
Run backend
```bash
cd backend_python
.\venv\Scripts\activate
python run.py
```

✅ If successful, you should see:
```bash
Checking if database tables exist...
Database tables created successfully.
 * Serving Flask app 'app'
 * Debug mode: on
 * Running on http://127.0.0.1:5000
 * Running on http://192.168.1.x:5000
Press CTRL+C to quit
 * Debugger is active!
```

📱 Frontend Setup (Android)
➡️ Open Project
Open the frontend_java folder in Android Studio.

➡️ Configure Backend URL
Edit the file frontend_java/local.properties and add:

```bash
BASE_URL=http://192.168.1.x:5000
```

📌 Make sure to replace 192.168.1.x with your actual local IP address (same IP where backend is running).

## Account
```bash
- User: 
username: user
password: 123456

- Admin
username: admin
password: admin
```



✅ Notes
Ensure the database is created before running the backend.

This backend uses Flask development server — for production use, consider deploying with a WSGI server like gunicorn or uWSGI.

Frontend is built using Java + Android Studio.


