# WeTalk ChatProject - Folder Structure

## Overview
This document outlines the folder structure of the WeTalk chat application project.

## Project Structure

```
ChatProject/
├── .DS_Store                           # macOS system file (ignore)
├── README.md                           # Project documentation
├── FOLDER_STRUCTURE.md                 # This file
├── mysql-connector-java-8.0.33.jar    # MySQL database connector
├── chat_log.txt                        # Chat log file
├── command.txt                         # Command reference file
│
├── Main Application Files (Root Level)
├── ChatClientGUI.java                  # Legacy GUI client implementation
├── ChatClientGUI.class                 # Compiled class file
├── Client.java                         # Client implementation
├── Client.class                        # Compiled class file
├── Server.java                         # Server implementation
├── Server.class                        # Compiled class file
├── ClientHandler.java                  # Client connection handler
├── ClientHandler.class                 # Compiled class file
├── DatabaseHandler.java               # Database operations handler
├── DatabaseHandler.class              # Compiled class file
├── DatabaseTest.java                  # Database testing utilities
├── DatabaseTest.class                 # Compiled class file
│
├── Additional Compiled Classes (ChatClientGUI inner classes)
├── ChatClientGUI$1.class
├── ChatClientGUI$1ModernScrollBarUI.class
├── ChatClientGUI$2.class
├── ChatClientGUI$3.class
├── ChatClientGUI$4.class
├── ChatClientGUI$ChatBubblePanel.class
├── ChatClientGUI$IncomingReader.class
├── ChatClientGUI$ModernScrollBarUI.class
├── ChatClientGUI$RoundedButton.class
├── ChatClientGUI$StyledTextField.class
│
└── com/                                # Package structure (MVC Architecture)
    └── chat/
        └── client/
            ├── controller/             # Controller Layer (MVC)
            │   ├── ChatClientController.java
            │   └── ChatClientController.class
            │
            ├── model/                  # Model Layer (MVC)
            │   ├── ChatClientModel.java
            │   ├── ChatClientModel.class
            │   ├── ChatClientModel$IncomingReader.class
            │   ├── ChatMessage.java
            │   ├── ChatMessage.class
            │   ├── ChatMessage$MessageType.class
            │   ├── DatabaseAuthenticator.java
            │   ├── DatabaseAuthenticator.class
            │   ├── User.java
            │   └── User.class
            │
            └── view/                   # View Layer (MVC)
                ├── ChatClientView.java
                ├── ChatClientView.class
                ├── ChatClientView$1.class
                └── ChatClientView$2.class
```

## Architecture Overview

### Root Level Files
- **Server Components**: `Server.java`, `ClientHandler.java` - Handle server-side operations
- **Legacy Client**: `ChatClientGUI.java` - Original GUI implementation
- **Simple Client**: `Client.java` - Basic client implementation
- **Database**: `DatabaseHandler.java`, `DatabaseTest.java` - Database operations
- **Dependencies**: `mysql-connector-java-8.0.33.jar` - MySQL connector

### MVC Package Structure (`com.chat.client`)

#### Controller (`com.chat.client.controller`)
- `ChatClientController.java` - Main application controller logic

#### Model (`com.chat.client.model`)
- `ChatClientModel.java` - Client data model and business logic
- `ChatMessage.java` - Message data structure
- `DatabaseAuthenticator.java` - Database authentication
- `User.java` - User data model

#### View (`com.chat.client.view`)
- `ChatClientView.java` - User interface components

## File Types
- `.java` - Java source code files
- `.class` - Compiled Java bytecode files
- `.jar` - Java library (MySQL connector)
- `.txt` - Text files (logs, commands)

## Notes
- The project uses both legacy code (root level) and modern MVC architecture (com.chat.client package)
- All `.class` files are compiled versions of the corresponding `.java` files
- Inner classes are represented with `$` notation in compiled files
