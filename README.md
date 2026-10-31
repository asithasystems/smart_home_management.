Smart Home Management System

Web application to control a smart home. Users register and log in, create rooms, add devices to rooms, switch devices ON and OFF, edit and delete devices and view the total power consumption on a real-time dashboard. All data is permanently stored in a MySQL database.
The power values are software simulated: They are calculated from the rated power (in watts) of all devices that are switched ON. No physical hardware is used.


Features

 Accounts - Sign up and log in. The actual password is not stored,  it is hashed using BCrypt.
 
 Rooms - Create rooms, view them as cards, delete rooms 

 Devices - Create devices (name, type, room, rated power, state),   view them in a table, edit them, switch ON/OFF, delete them
 
 Dashboard - Device and room counts, and a Live Power Consumption  card that fetches the power consumption from the API endpoint `/api/power/live` 
 
 Validation - Browser checks, server checks (Bean Validation),  database constraints, with clear error messages 

 Responsive design - layout adjusts to phone size screens 

 About page - Information about the developer and the project 
 


Technologies

 Frontend - HTML, CSS, and JavaScript.Database - SQL Server. 
 Access to the database - Spring Data JPA with Hibernate 
 Database - MySQL 
 Backend - Spring, Spring Boot, and MySQL 
 Security - Spring Security, BCrypt 
 Build tool - Maven 
 Tests - JUnit and Spring Boot Test (H2 in-memory database)



Project structure


smart-home-management/
├── pom.xml
├── README.md
├── docs/
│   ├── erd.png
│   └── screenshots/
└── src/
    ├── main/
    │   ├── java/com/example/smarthome/
    │   │   ├── config/        (SecurityConfig)
    │   │   ├── controller/    (Auth, Home, Device, RoomSetup, PowerApi)
    │   │   ├── dto/
    │   │   ├── entity/        (AppUser, Room, Device, TelemetryReading, enums)
    │   │   ├── repository/
    │   │   ├── service/
    │   │   └── SmartHomeApplication.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/        (css, images)
    │       └── templates/     (login, register, index, rooms, devices, edit-device, about)
    └── test/                  (3 test classes)





Setup and run

Requirements
 JDK 17
 Maven 3.6 or newer
 Local MySQL Server.

1. Create the database

In MySQL Workbench or the MySQL command line:

If the database does not exist, create it.Create the database if it doesn't exist:
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

The tables are created automatically the first time the application starts.


2. Configure the database login.

The application uses environment variables to read its settings: the default value for `DB_USER` is `root`, and the value of `DB_PASS` is required. For Windows PowerShell (for the current window):

powershell
$env:DB_USER = "root"
$env:DB_PASS = "YOUR_MYSQL_PASSWORD"

On macOS or Linux: Set the DB_USER to root and DB_PASS to YOUR_MYSQL_PASSWORD.

You can optionally change the value of `DB_URL` to point to a different database address. There is no password for this repository.


3. Run the application

Open a terminal in the folder that contains `pom.xml`:

mvn spring-boot:run

Next, go to http://localhost:8080 in a browser. Click on Create an account, register a user and log in. Add devices to a room after creating the room.

4. Check the saved data

sql
USE smart_home_db;
SELECT * FROM app_users;
SELECT * FROM rooms;
SELECT * FROM devices;


5. Run the tests

bash
mvn test

The tests use an in-memory H2 database, so they do not touch your MySQL data.


Limitations and future work.

 The power readings are simulated from device ratings. No actual device is attached.
 Planned: real hardware (e.g. ESP32 via MQTT or HTTP), real readings in `telemetry_readings`, power history charts, admin and normal user roles.


Author

Asitha Dananjaya Weerakoon,
Electrical & Information Engineering Undergraduate,
University of Ruhuna, Sri Lanka

