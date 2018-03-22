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
* xml-json-to-avro-mapper

# Run the Adapter
* run die CISRestAdapter class as java application

## DOCKER will be soon available
* Run docker container

# Swagger Interface for testing
The REST Adapter offers for testing purposes a swagger ui where all exposed methods can be tested.
The SWAGER UI can be reached by:
*http://localhsot:8090/swagger-ui.html

# REST Endpoint callback
If the application wants to get the received messages via a RESTEndpoint (provided by the application), this can
be configured by sending the URL to the addRESTEndpoint:

## Example Request http POST to
http://localhost:8090/CISRestAdaptor/addRESTEndpoint?url=http%3A%2F%2Flocalhost%3A8090%2FrestCallbackEndpoint

e.g.:curl -X POST "http://localhost:8090/CISRestAdaptor/addRESTEndpoint?url=http%3A%2F%2Flocalhost%3A8090%2FrestCallbackEndpoint" -H "accept: */*"

# WS callback
An alternative way of getting the received messages is a websocket communication.
This is the prio1 way for communication, if you sepcific both, the websocket will be used.

WS endpoint:
ws://localhost:8090/RESTAdaptorWSEndpoint

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
*server.port = 8090

### client.properties
*client.id=any unique id

Be sure the the id you are using is unique


## Specific Configuration

If you wish to override default configuration values you can do so in the configuration files in the 'config' directory.
