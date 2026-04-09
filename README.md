[README.md](https://github.com/user-attachments/files/26593173/README.md)
# 🏥 BUET MedTech — Hospital Management System

A full-featured desktop hospital management application built with **JavaFX** and **SQLite**,
designed for seamless management of doctors, patients, appointments, prescriptions, and more —
all from a single, beautiful offline desktop app.

---

## 📋 Table of Contents

- [About](#about)
- [Benefits](#benefits)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation Guide (EXE)](#installation-guide-exe)
- [Running from Source](#running-from-source)
- [Project Structure](#project-structure)
- [Screenshots](#screenshots)

---

## About

BUET MedTech is a role-based hospital management system built as a desktop application.
It supports three user roles — **Admin**, **Doctor**, and **Patient** — each with their own
dedicated dashboard and feature set. The application runs entirely offline using a local
SQLite database, making it fast, private, and easy to deploy.

---

## ✅ Benefits

- **Completely offline** — no internet required, all data stored locally and securely
- **Role-based access** — Admin, Doctor, and Patient each see only what's relevant to them
- **No monthly fees** — install once, use forever, no subscriptions
- **Fast and lightweight** — SQLite database, no heavy server setup needed
- **Professional UI** — clean modern interface with Light and Dark mode support
- **All-in-one** — appointments, prescriptions, reports, billing, Q&A, blood donation in one app
- **Easy to deploy** — ships as a single `.exe` installer with no extra dependencies

---

## ✨ Features

### 🔐 Authentication & Onboarding
- Role selection screen (Admin / Doctor / Patient)
- Email or username login with password
- New user registration with role assignment
- Profile setup wizard for Doctors and Patients after first login
- Background-threaded login with "Connecting..." indicator so the UI never freezes
- Keyboard shortcuts: **Enter** to login, **Escape** to go back
- Animated splash screen on launch

---

### 🛡️ Admin Dashboard

| Feature | Details |
|---|---|
| **Dashboard Statistics** | Total doctors, patients, appointments, completed, pending, total reports, total revenue |
| **Manage Doctors** | Add new doctors with full details, search by name/email/specialization, delete doctors |
| **Manage Patients** | View all patients, search by name/email/phone, delete patients |
| **Manage Appointments** | View all appointments, filter by doctor/patient/date/status, update appointment status |
| **Reports** | View all medical reports across the system with patient and doctor info |
| **Transactions** | Track appointment-based revenue, doctor fees, hospital share (30%), total earnings |
| **Notifications** | Broadcast notifications to all users at once with category tagging |
| **Monitoring** | Monitor blood donors, doctor health posts, and patient Q&A activity system-wide |

---

### 👨‍⚕️ Doctor Dashboard

| Feature | Details |
|---|---|
| **Dashboard Overview** | Today's appointments, total patients treated, pending reports, total income, health posts count, answered Q&A count |
| **Appointments** | View all appointments with filter by status (upcoming/confirmed/completed/cancelled), update appointment status |
| **My Patients** | View all patients treated, search by name |
| **Write Medical Report** | Select completed appointment, fill report name, lab tests, diagnosis, doctor notes, follow-up advice, save report |
| **Prescriptions** | Select appointment, add medicines with dosage/duration/instructions, preview full prescription |
| **Health Tips** | Create health posts with title, category and content, view all own posts |
| **Q&A Forum** | View unanswered patient questions, submit detailed answers |
| **My Profile** | Update specialization, qualification, experience, phone, consultation fee |
| **Recent Notifications** | View latest notifications from admin |
| **Light / Dark Mode** | Toggle between light and dark theme |

---

### 🧑‍💼 Patient Dashboard

| Feature | Details |
|---|---|
| **Dashboard Overview** | Upcoming appointments count, total reports, unread notifications, blood donation status, recent notifications, recent posts, recent Q&A, recent donors |
| **Search Doctors** | Browse all doctors with specialization, fee, experience; filter by name/specialization/fee range |
| **Book Appointment** | Select doctor, pick date and time slot, enter visit reason, submit appointment |
| **View Appointments** | View all appointments with filter (upcoming/confirmed/completed/cancelled), cancel appointments |
| **Medical Reports** | View all reports written by doctors, with diagnosis, prescription, lab tests, notes, follow-up |
| **Notifications** | View all notifications from admin, mark as read |
| **Doctor Posts** | Read health tips and articles posted by doctors, filter by category |
| **Q&A Forum** | Ask medical questions, view own questions and doctor answers |
| **Blood Donation** | Register as a blood donor, search donors by blood group/location, view donor availability |
| **Smart Assistant** | AI-powered symptom analyzer — enter symptoms and get possible conditions and advice |
| **Profile Settings** | Update phone, blood group, address, allergies, diabetes status, medical history |
| **Light / Dark Mode** | Toggle between light and dark theme |

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17+ |
| UI Framework | JavaFX 21 |
| Database | SQLite (via JDBC) |
| Build Tool | Maven |
| Styling | CSS (JavaFX stylesheets) |
| Packaging | jpackage (JDK built-in) |

---

## 📦 Installation Guide (EXE)

Follow these steps to build and install BUET MedTech as a Windows `.exe`.

### Prerequisites

Make sure you have the following installed:

1. **JDK 17 or higher**
   Download from: https://adoptium.net
   After installing, verify: `java -version`

2. **Maven 3.8+**
   Download from: https://maven.apache.org/download.cgi
   After installing, verify: `mvn -version`

3. **WiX Toolset v3** *(required by jpackage to build .exe)*
   Download from: https://wixtoolset.org/releases
   Install it, then **restart your PC** so it's added to PATH.

---

### Step 1 — Add the jpackage plugin to `pom.xml`

Open your `pom.xml` and add this inside `<build><plugins>`:

```xml
<plugin>
    <groupId>org.panteleyev</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>1.6.0</version>
    <configuration>
        <name>BUETMedTech</name>
        <appVersion>1.0.0</appVersion>
        <vendor>BUET</vendor>
        <mainClass>com.example.project.Launcher</mainClass>
        <mainJar>project-1.0-SNAPSHOT.jar</mainJar>
        <type>EXE</type>
        <winDirChooser>true</winDirChooser>
        <winShortcut>true</winShortcut>
        <winMenu>true</winMenu>
        <destination>target/installer</destination>
    </configuration>
</plugin>
```

> ⚠️ Change `<mainJar>` to match the actual JAR name in your `target/` folder after building.

---

### Step 2 — Build the project

Open a terminal in your project root folder and run:

```bash
mvn clean package
```

This compiles everything and produces a JAR in the `target/` folder.

---

### Step 3 — Generate the EXE installer

```bash
mvn jpackage:jpackage
```

This will create the installer at:

```
target/installer/BUETMedTech-1.0.0.exe
```

---

### Step 4 — Install the app

1. Double-click `BUETMedTech-1.0.0.exe`
2. Follow the installer wizard
3. Choose installation directory
4. A desktop shortcut and Start Menu entry will be created automatically
5. Launch **BUET MedTech** from the desktop or Start Menu

---

### Step 5 — First Launch

On first launch, the app will:
- Automatically create the `hospital.db` SQLite database in the installation folder
- Set up all required tables
- Create a default Admin account:

```
Email:    admin@buet.ac.bd
Password: admin123
Role:     Admin
```

> 🔒 Change the admin password after first login.

---

## 🚀 Running from Source

If you want to run the project directly without building an EXE:

```bash
# Clone or open the project in IntelliJ IDEA / VS Code

# Run using Maven
mvn clean javafx:run
```

Make sure your `pom.xml` includes the `javafx-maven-plugin`.

---

## 🗂️ Project Structure

```
src/
└── main/
    ├── java/com/example/project/
    │   ├── Launcher.java                    # Entry point
    │   ├── MainApp.java                     # JavaFX Application start
    │   ├── controller/
    │   │   ├── SplashController.java
    │   │   ├── RoleSelectionController.java
    │   │   ├── LoginController.java
    │   │   ├── RegisterController.java
    │   │   ├── ProfileSetupController.java
    │   │   ├── AdminDashboardController.java
    │   │   ├── DoctorDashboardController.java
    │   │   └── PatientDashboardController.java
    │   └── util/
    │       ├── DatabaseConnection.java      # SQLite connection
    │       ├── DBInitializer.java           # Table creation on startup
    │       ├── SceneManager.java            # Scene switching & window sizing
    │       └── SessionManager.java          # Logged-in user session
    └── resources/
        ├── fxml/                            # All UI layout files
        ├── css/                             # Stylesheets (light + dark theme)
        └── images/                          # App logo and icons
```

---

## 👥 Default Roles & Access

| Role | Access |
|---|---|
| **Admin** | Full system control — manage users, appointments, reports, billing, notifications |
| **Doctor** | Own appointments, patients, reports, prescriptions, health posts, Q&A |
| **Patient** | Book appointments, view reports, Q&A, blood donation, smart assistant |

---

## 📄 License

This project was developed as part of an academic project at **BUET (Bangladesh University of Engineering and Technology)**.
Free to use for educational purposes.

---

*Built with ❤️ by BUET Students*
