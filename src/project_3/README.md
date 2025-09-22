# CS_316 – Project 3: TCP File Transfer

This is **Project 3** for CS 316. It demonstrates Java networking concepts using a simple client–server application for transferring files over TCP.

## Repository Structure

```text
project_3/
├── tcp_file_client.java   # Client program that sends files to the server
├── tcp_file_server.java   # Server program that receives files from clients
```

## Features

- Java socket programming
- TCP client–server architecture
- File transfer from client to server
- Basic error handling for network and file I/O

## Prerequisites

- Java JDK 17+
- IntelliJ IDEA or another Java IDE
- Command line (optional) to compile/run without IDE

## Getting Started

```bash
cd src/project_3 #Navigate into the Project 3 directory
javac tcp_file_client.java tcp_file_server.java #Compile the files
java tcp_file_server #Run the server
java tcp_file_client #In another terminal, run the client
```