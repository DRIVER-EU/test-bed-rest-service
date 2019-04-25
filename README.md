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

# Topic subscription for receiving messages
In order to receive messages from different topics, you need to subscribe on them. This can be done via the REST Endpoint:

## subscribeOnTopic
http://localhost:8190/CISRestAdaptor/subscribeOnTopic?topic=<topicName>

## JSON format of received message:
When a message is received on a subscribed topic it will be enveloped into a JOSN structure and forwareded to the client via the registered Callback (REST) or via the WebSocket connection:
{
	"header": {
				"topic": <topicName of the received message>,
				"senderId": <clientId of the sending solution>
			  },
	"payload", <the received message>
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

## Configuration

###Default Consumer Properties
* bootstrap.servers=broker.url
* group.id=<client.id> from the client-config.properties files
* enable.auto.commit=true
* auto.offset.reset=latest
* key.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
* value.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
* schema.registry.url=schema.url

###Default Consumer Properties
* bootstrap.servers=broker.url
* schema.registry.url=schema.url
* compression.type=none
* acks=all
* retries=retry.count
* request.timeout.ms= retry.time
* key.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
* value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer

## Authentication Configuration
If the Testbed is running in seucred mode, the adapter needs to identify with a certificate. This certificate has to be stored and the needed information have to be provided in the ssl.properties file. The adapter will automatically detect if ssl is needed.
* security.protocol=SSL
* ssl.truststore.location=config/cert/truststore.jks
* ssl.truststore.type=JKS
* ssl.truststore.password=changeit
* ssl.keystore.location=config/cert/test_new.p12
* ssl.keystore.type=PKCS12
* ssl.keystore.password=test
* ssl.key.password=test


#### Run the REST adapter

Then, run the .bat file or the java command inside:

\test-bed-rest-service-master\executable>java -jar rest-testbed-adapter-1.2.6.jar

# Step by Step
## start the adapter:
 \executable>java -jar rest-testbed-adapter-1.2.6.jaras soon as the adapter is up

## you have 2 options to receive messages:
* register a REST Endpoint callback: http://localhost:8190/CISRestAdaptor/addRESTEndpoint?url=<URL to the endpoint> or * connect to the WebSocket and run the heartbeat protocol to keep the Websocket upNext step is, 

## you need to subscribe to the topics from which you want to receive messages
* http://localhost:8190/CISRestAdaptor/subscribeOnTopic?topic=<topicName>

without this, you will not receive any messages. As soon as you receive a message you will get a json message (on the REST or the WebSocket) which detailed information and the message itself
{
    "header": {
    	          "topic": <topicName of the received message>,
    	          "senderId": <clientId of the sending solution>
    	      },
    "payload", <the received message>
}