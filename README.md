# QueueMate

Barangay Service Queue Management System — a beginner-friendly Java Swing GUI prototype for managing residents requesting barangay services.

## Features
- Register residents and select a barangay service
- Generate queue numbers and resident IDs
- Call the next waiting resident
- Mark the current transaction as completed
- View waiting and completed transactions in separate tabs
- Dashboard counters for total, waiting, serving, and completed residents
- Save and reload queue records using a local text file

## Services
- Barangay Clearance
- Certificate of Residency
- Certificate of Indigency

## Requirements
- Java JDK installed (includes `javac`)
- Java Swing (included in standard Java desktop installations)

## Run the program
1. Download or clone this repository.
2. Open a terminal in the project folder.
3. Compile:

   ```bash
   javac QueueMate.java
   ```

4. Run:

   ```bash
   java QueueMate
   ```

QueueMate saves its records in `queuemate_records.txt` in the working directory. The included text file contains sample records.

## Project structure
```text
QueueMate/
├── QueueMate.java
├── queuemate_records.txt
└── README.md
```


