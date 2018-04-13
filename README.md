# REST Endpoint Adapter
This standalone DRIVER+ Adapter provides an REST Endpoint for sending standard message (e.g. CAP, MLP) via the JAVA Testbed Adapter.
The standard message can be validated before sending it to the CIS. This mechanism can be configured in the Adaptor properties. If the parameter is not specified no validation is performed.
It offers two possibilities for retrieving messages back:
REST Endpoint: can be configured in the adaptor properties or by passing it via a REST Endpoint
WebSocket: established by the client, adaptor is sending the message via this socket communication (heartbeat - invoked by the client (request-response) needed to check if the socket is up).

# Requirements for Development

* Java JDK 1.8+
* Development environment e.g. Eclipse
* maven
* java-testbed-adaptor

# Run the Adapter
* run die CISRestAdapter class as java application

## Executable
* run the REST Service by calling the bat file in the executable directory

## DOCKER will be soon available
* Run docker container

# Swagger Interface for testing
The REST Adapter offers for testing purposes a swagger ui where all exposed methods can be tested.
The SWAGER UI can be reached by:
* http://localhost:8190/swagger-ui.html

# REST Endpoint callback
If the application wants to get the received messages via a RESTEndpoint (provided by the application), this can
be configured by sending the URL to the addRESTEndpoint:

## Example Request http POST to
http://localhost:8190/CISRestAdaptor/addRESTEndpoint?url=http%3A%2F%2Flocalhost%3A8090%2FrestCallbackEndpoint

e.g.:curl -X POST "http://localhost:8190/CISRestAdaptor/addRESTEndpoint?url=http%3A%2F%2Flocalhost%3A8090%2FrestCallbackEndpoint" -H "accept: */*"

# WS callback
An alternative way of getting the received messages is a websocket communication.
This is the prio1 way for communication, if you specify both, the websocket will be used.

WS endpoint:
ws://localhost:8190/RESTAdaptorWSEndpoint

## Hearbeat on websocket to check the connectivity
to check if the connection is up and running, a regular heartbeat has to be send. This will be answered by the Server:

### JSON heartbeat request/response:
{
  "requestId" : "1224-at56-7890-atgf",
  "type" : "eu.driver.adaptor.ws.request.heartbeat",
  "sendTime" : 1520502643030
}
{
  "requestId" : "1224-at56-7890-atgf",
  "type" : "eu.driver.adaptor.ws.response.heartbeat",
  "sendTime" : 1520502661640,
  "state" : "OK"
}

# Configuration

## Default values
### application.properties
* server.port = 8190

### client.properties
* client.id=any unique id

Be sure the the id you are using is unique


## Specific Configuration

If you wish to override default configuration values you can do so in the configuration files in the 'config' directory.

# Trial 1 specific "Data Update Tool"
## Goal:

To provide an easy way of notifying others that there is data available for which it would not be feasible to send it over the test-bed.

## Solution:

A simple tool, consisting of only one REST endpoint (http://localhost:8190/CISRestAdaptor/sendLargeDataUpdateJson) and a basic UI
The UI provides a form where the location of the data (an URL) as well as the data type and a title must be filled in. Optionally, a description might be entered.

## HowTo:

#### Configuration

*Initial configuration:*

This tool will notify the Testbed about a very specific event, hence, the "target" Testbed parameters must be set as follows:

File \test-bed-rest-service-master\config\producer.properties:

- bootstrap.servers=<TESTBED_SOCKET>
- schema.registry.url=<URL>
  
File \test-bed-rest-service-master\config\consumer.propierties:
  
-	bootstrap.servers=<TESTBED_SOCKET>
-	schema.registry.url=<SCHEMA_URL>
-	group.id=<CLIENT_ID>

File \test-bed-rest-service-master\config\client.properties:
-	client.id=<CLIENT_ID>

*Being:*
 <TESTBED_SOCKET>, the testbed socket point in socket format (IP:port)
 <SCHEMA_URL>, the testbed schema url address in URL format (http://IP:port)
 <CLIENT_ID>, client_id defined in the testbed configuration where the app points out.

#### Run the REST adapter

Then, run the .bat file or the java command inside:

\test-bed-rest-service-master\executable>java -jar rest-testbed-adapter-1.0.3.jar

#### Usage of the form

Once it is running, they should be able to open the url http://localhost:8190/
Then the notification form should appear.
