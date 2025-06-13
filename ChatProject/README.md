# WeTalk - Simple Chat Application

A basic real-time chat application built with Java, featuring both legacy and MVC implementations.

## Overview

WeTalk is a learning project that demonstrates socket programming and GUI development in Java. It includes two different client implementations:
- Legacy implementation with all code in a single file
- Modern MVC architecture with separated concerns

## Features

- Real-time messaging using Java sockets
- User authentication with MySQL database
- Multiple client support
- Chat logging
- Two different client implementations for comparison

## Tech Stack

- **Language:** Java
- **GUI:** Java Swing
- **Database:** MySQL
- **Networking:** Java Socket API

## Project Structure

```
WeTalk/
├── README.md
├── FOLDER_STRUCTURE.md
├── mysql-connector-java-8.0.33.jar    # MySQL connector
├── chat_log.txt                        # Server chat logs
├── command.txt
│
├── Server Components
├── Server.java                         # Main server
├── ClientHandler.java                  # Handles client connections
├── DatabaseHandler.java               # Database operations
├── DatabaseTest.java                  # Database testing
│
├── Legacy Client Implementation
├── ChatClientGUI.java                 # Single-file GUI client
├── Client.java                        # Simple console client
│
└── MVC Client Implementation (com/chat/client/)
    ├── controller/
    │   └── ChatClientController.java   # Main controller
    ├── model/
    │   ├── ChatClientModel.java        # Client logic
    │   ├── ChatMessage.java            # Message structure
    │   ├── DatabaseAuthenticator.java  # Auth logic
    │   └── User.java                   # User model
    └── view/
        └── ChatClientView.java         # UI components
```

## Setup and Installation

### Prerequisites
- Java JDK 8 or higher
- MySQL Server
- MySQL Connector/J (included in project)

### Database Setup
1. Create MySQL database:
   ```sql
   CREATE DATABASE we_talk;
   USE we_talk;
   
   CREATE TABLE users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(50) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

2. Update database credentials in `DatabaseHandler.java`:
   ```java
   private static final String DB_URL = "jdbc:mysql://localhost:3306/we_talk";
   private static final String DB_USER = "your_username";
   private static final String DB_PASSWORD = "your_password";
   ```

### Compilation
```bash
# Windows
javac -cp "mysql-connector-java-8.0.33.jar;." *.java com/chat/client/*.java com/chat/client/*/*.java

# Linux/Mac
javac -cp "mysql-connector-java-8.0.33.jar:." *.java com/chat/client/*.java com/chat/client/*/*.java
```

## Usage

### Starting the Server
```bash
# Windows
java -cp "mysql-connector-java-8.0.33.jar;." Server

# Linux/Mac  
java -cp "mysql-connector-java-8.0.33.jar:." Server
```
The server runs on port 1234 by default.

### Running Clients

#### Option 1: Legacy GUI Client
```bash
# Windows
java -cp "mysql-connector-java-8.0.33.jar;." ChatClientGUI

# Linux/Mac
java -cp "mysql-connector-java-8.0.33.jar:." ChatClientGUI
```
- Single file implementation
- All GUI code in one class
- Good for understanding the basics

#### Option 2: MVC GUI Client
```bash
java -cp "mysql-connector-java-8.0.33.jar;." com.chat.client.controller.ChatClientController
```
- Separated Model-View-Controller architecture
- Better code organization
- Easier to maintain and extend

#### Option 3: Console Client
```bash
java Client
```
- Simple text-based interface
- No database authentication
- Basic functionality only

### Client Features Comparison

| Feature | Legacy GUI | MVC GUI | Console |
|---------|------------|---------|---------|
| Authentication | ✅ | ✅ | ❌ |
| GUI Interface | ✅ | ✅ | ❌ |
| Real-time Chat | ✅ | ✅ | ✅ |
| Custom Styling | ✅ | ✅ | ❌ |
| MVC Architecture | ❌ | ✅ | ❌ |
| Easy to Extend | ❌ | ✅ | ❌ |

## How It Works

1. **Server**: Listens for client connections and manages chat rooms
2. **Authentication**: Clients authenticate against MySQL database
3. **Messaging**: Real-time communication using Java sockets
4. **Logging**: All messages are logged to `chat_log.txt`

## Learning Objectives

This project demonstrates:
- Socket programming in Java
- Basic GUI development with Swing
- Database integration
- MVC design pattern
- Multithreading for client handling

## Notes

- The legacy implementation shows how NOT to structure larger applications
- The MVC implementation demonstrates better software engineering practices
- Both implementations connect to the same server and can chat with each other
