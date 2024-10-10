# CollaboratorFetcher

## Description
**CollaboratorFetcher** is a Java-based Burp Suite extension built using the `Montoya API`. It enables opening a web application API interface between Burp Suite's Collaborator and your local machine.

I developed this extension to facilitate interaction with the Collaborator server using Python scripts and tools. Since direct communication with the Collaborator is only possible through Java-based extensions, **CollaboratorFetcher** provides an easy workaround. By loading this extension in Burp Suite, you can develop your Python scripts, interact with Collaborator via a web API, and seamlessly handle HTTP and DNS responses.

## Features 
- **Web Application API:** Provides a seamless interface for interacting with the Collaborator.
- **Customizable Payload Requests:** Allows users to request a specific number of Collaborator payloads.
- **JSON Format Interaction Retrieval:** Facilitates the retrieval of interactions in a structured JSON format.

## Installation (From Packaged JAR Files)
This tool consists of two JAR files: `CollaboratorFetcher-1.0.jar` (the extension) and `WebApplicationInterface-1.0.jar` (the web application interface).

1. **Copy `WebApplicationInterface-1.0.jar`:** Place this file in the Burp Suite directory, next to the Burp Suite JAR file.
2. **Load `CollaboratorFetcher-1.0.jar`:** Import this file as a Java extension in Burp Suite.

`CollaboratorFetcher-1.0.jar` will launch the web application server on port `8888`. Note that you cannot change the port number or the names of the JAR files unless you compile the source code yourself after making your intended modifications. We will discuss this process in the next section.

## API Overview

This API allows interaction with the **CollaboratorFetcher** tool, providing endpoints to manage payloads and interactions. Below is a description of each available endpoint.

### Base URL

The base URL for the API is `http://localhost:8888` (or your configured port).

### Endpoints

#### 1. **GET /** - Main Page
- **Description:** Returns a welcome message.
- **Response:**
  ```json
  {
    "message": "Welcome to the API! Use /start to initialize payloads."
  }
  ```

#### 2. **GET /start** - Initialize Payloads
- **Description:** Initializes the number of payloads.
- **Parameters:**
  - `total-payloads`: The intended number of payloads to initialize.
- **Usage:** 
  ```http
  GET /start?total-payloads=5
  ```
- **Response:**
  ```json
  {
    "num-payloads": "5"
  }
  ```
- **Note:** You can only set the number of payloads once. Subsequent attempts will return an error message.

#### 3. **GET /get-payloads** - Retrieve All Payloads
- **Description:** Returns a list of all added payloads.
- **Usage:** 
  ```http
  GET /get-payloads
  ```
- **Response:**
  ```json
  {
    "1": "payload1",
    "2": "payload2"
  }
  ```

#### 4. **GET /get-interactions** - Retrieve All Interactions
- **Description:** Returns a list of all added interactions.
- **Usage:** 
  ```http
  GET /get-interactions
  ```
- **Response:**
  ```json
  {
    "1-payloadID-protocol": "base64EncodedResponse",
    "2-payloadID-protocol": "base64EncodedResponse"
  }
  ```

## JAR Packaging (Using Maven)
### Prerequisites:
Make sure you have `Java 17` installed and set up on your machine. You can verify this by running:
```shell
java -version
```
### Steps to Install Maven
```shell
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz

tar -xzf apache-maven-3.9.9-bin.tar.gz

export PATH=$PWD/apache-maven-3.9.9/bin:$PATH
```
### Packaging the Source Code
```shell
git clone https://github.com/ma4747gh/CollaboratorFetcher

cd CollaboratorFetcher/CollaboratorFetcher

mvn clean package

cd ../WebApplicationInterface/

mvn clean package
```
### Important Modifications
#### Port Number:
1. Open the file `WebApplicationInterface/src/main/resources/application.properties`.
2. Locate the line `server.port=8888` and change it to your desired port number.
#### WebApplicationInterface-1.0.jar Path:
If you prefer not to copy `WebApplicationInterface-1.0.jar` to the Burp Suite directory, you can simply edit the path to load the JAR file.
1. Open the file `CollaboratorFetcher/src/main/java/example/fetcher/collaborator/CollaboratorFetcher.java`.
2. Locate the line `"java", "-jar", "WebApplicationInterface-1.0.jar"` and change `WebApplicationInterface-1.0.jar` to the desired path, such as `~/Desktop/WebApplicationInterface-1.0.jar`. After packaging, ensure that `WebApplicationInterface-1.0.jar` is placed in the specified location.

## Contributing
If you'd like to contribute, feel free to submit a pull request or open an issue for bug reports and suggestions.

## Contact
For feedback or issues, please reach out via **ma4747gh@gmail.com** or submit an issue on GitHub.

[My LinkedIn](https://www.linkedin.com/in/ma4747gh/)