package eu.driver.adaptor.controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import eu.driver.adapter.core.CISAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adaptor.callback.AdapterCallback;
import eu.driver.adaptor.mapper.cap.XMLToAVROMapper;
import eu.driver.model.core.DataType;
import eu.driver.model.core.LargeDataUpdate;
import eu.driver.model.core.LayerType;
import eu.driver.model.core.Level;
import eu.driver.model.core.Log;
import eu.driver.model.core.MapLayerUpdate;
import eu.driver.model.core.UpdateType;
import eu.driver.model.geojson.FeatureCollection;
import eu.driver.model.geojson.GeoJSONEnvelope;

@RestController
public class SendRestController implements
		ResourceProcessor<RepositoryLinksResource> {

	private Logger log = Logger.getLogger(this.getClass());
	private XMLToAVROMapper avroMapper = new XMLToAVROMapper();
	private CISAdapter adapter = CISAdapter.getInstance();
	
	private Map<String, String> registeredTopics = new HashMap<String, String>();

	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		resource.add(ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(SendRestController.class)
						.sendXMLMessage("CAP", "defaultCGOR", "XML")).withRel(
				"sendXMLMessage"));
		return resource;
	}

	public SendRestController() {
		/*this.adapter.addCallback(new AdapterCallback(TopicConstants.STANDARD_TOPIC_CAP),
				TopicConstants.STANDARD_TOPIC_CAP);
		this.adapter.addCallback(new AdapterCallback(TopicConstants.STANDARD_TOPIC_EMSI),
				TopicConstants.STANDARD_TOPIC_EMSI);*/
	}

	@ApiOperation(value = "sendXMLMessage", nickname = "sendXMLMessage")
	@RequestMapping(value = "/CISRestAdaptor/sendXMLMessage/{type}", method = RequestMethod.POST, consumes = { "application/xml" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "type", value = "the type of the xml content", required = true, dataType = "string", paramType = "path", allowableValues = "CAP, MLP, GEOJSON, EMSI"),
			@ApiImplicitParam(name = "cgorName", value = "name of the cgor, if not provided, default public distribution group is used", required = false, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "xmlMsg", value = "the XML message as string", required = true, dataType = "string", paramType = "body", example = "<Alert></Alert>") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Response.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Response.class),
			@ApiResponse(code = 500, message = "Failure", response = Response.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Response> sendXMLMessage(@PathVariable String type,
			@QueryParam("cgorName") String cgorName, @RequestBody String xmlMsg) {
		log.info("--> sendXMLMessage");
		log.debug(xmlMsg);

		Response response = new Response();
		GenericRecord avroRecord = null;
		// check message type
		if (type.equalsIgnoreCase("CAP")) {
			log.info("Processing CAP message.");
			avroRecord = avroMapper.convertCapToAvro(xmlMsg);

		} else if (type.equalsIgnoreCase("MLP")) {
			log.info("Processing MLP message.");
			avroRecord = avroMapper.convertMlpToAvro(xmlMsg);

		} else if (type.equalsIgnoreCase("GEOJSON")) {
			log.info("Processing GEOJSON message.");
			avroRecord = avroMapper.convertGeoJsonToAvro(xmlMsg);

		} else if (type.equalsIgnoreCase("EMSI")) {
			log.info("Processing EMSI message.");
			avroRecord = avroMapper.convertEMSIToAvro(xmlMsg);
		}

		if (avroRecord != null) {
			try {
				adapter.sendMessage(avroRecord);
				response.setMessage("The message was send successfully!");
			} catch (CommunicationException cEx) {
				log.error("Error sending the record!", cEx);
				response.setMessage("Error sending the record!");
				response.setDetails(cEx.getMessage());
				return new ResponseEntity<Response>(response,
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response.setMessage("Unknown message type!");
			return new ResponseEntity<Response>(response,
					HttpStatus.BAD_REQUEST);
		}

		log.info("sendXMLMessage -->");
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "sendLogMsg", nickname = "sendLogMsg")
	@RequestMapping(value = "/CISRestAdaptor/sendLogMsg", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "level", value = "level of the log record", required = true, dataType = "string", paramType = "query", allowableValues = "DEBUG, INFO, WARN, ERROR, CRITICAL, SILLY"),
			@ApiImplicitParam(name = "message", value = "level of the log record", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendLogMsg(
			@QueryParam("level") String level, @RequestBody String message) {
		log.info("--> sendLogMsg");
		Boolean send = true;

		Log logMsg = new Log();
		logMsg.setDateTimeSent(new Date().getTime());
		logMsg.setId(adapter.getClientID());
		logMsg.setLevel(Level.valueOf(level));
		logMsg.setLog(message);

		try {
			adapter.addLogEntry(logMsg);
		} catch (CommunicationException e) {
			log.error("Error sending the log request!");
		}

		log.info("sendLogMsg -->");
		return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendGeoJson", nickname = "sendGeoJson")
	@RequestMapping(value = "/CISRestAdaptor/sendGeoJson", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "cgorName", value = "name of the cgor, if not provided, default public distribution group is used", required = false, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "requestJson", value = "the XML message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendGeoJson(@QueryParam("cgorName") String cgorName, @RequestBody String requestJson ) {
		System.out.println(requestJson);
		log.info("--> sendGeoJson");

		try {
			InputStream input = new ByteArrayInputStream(requestJson.getBytes());
			DataInputStream din = new DataInputStream(input);
			Schema parsedSchema = FeatureCollection.SCHEMA$;
			Decoder decoder = DecoderFactory.get().jsonDecoder(parsedSchema, din);
		    DatumReader<GenericData.Record> reader = new GenericDatumReader(parsedSchema);
			adapter.sendMessage(reader.read(null, decoder), cgorName);
		} catch (CommunicationException cEx) {
			log.error("Error sending large data update message!", cEx);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception Ex) {
			log.error("Error sending large data update message!", Ex);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendGeoJson -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendNamedGeoJson", nickname = "sendNamedGeoJson")
	@RequestMapping(value = "/CISRestAdaptor/sendNamedGeoJson", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "cgorName", value = "name of the cgor, if not provided, default public distribution group is used", required = false, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "requestJson", value = "the XML message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendNamedGeoJson(@QueryParam("cgorName") String cgorName, @RequestBody String requestJson ) {
		System.out.println(requestJson);
		log.info("--> sendNamedGeoJson");

		try {
			InputStream input = new ByteArrayInputStream(requestJson.getBytes());
			DataInputStream din = new DataInputStream(input);
			Schema parsedSchema = GeoJSONEnvelope.SCHEMA$;
			Decoder decoder = DecoderFactory.get().jsonDecoder(parsedSchema, din);
		    DatumReader<GenericData.Record> reader = new GenericDatumReader(parsedSchema);
			adapter.sendMessage(reader.read(null, decoder), cgorName);
		} catch (CommunicationException cEx) {
			log.error("Error sending large data update message!", cEx);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception Ex) {
			log.error("Error sending large data update message!", Ex);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendNamedGeoJson -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendPhotoGeoJson", nickname = "sendPhotoGeoJson")
	@RequestMapping(value = "/CISRestAdaptor/sendPhotoGeoJson", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "cgorName", value = "name of the cgor, if not provided, default public distribution group is used", required = false, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "requestJson", value = "the XML message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendPhotoGeoJson(@QueryParam("cgorName") String cgorName, @RequestBody String requestJson ) {
		System.out.println(requestJson);
		log.info("--> sendPhotoGeoJson");

		try {
			InputStream input = new ByteArrayInputStream(requestJson.getBytes());
			DataInputStream din = new DataInputStream(input);
			Schema parsedSchema = eu.driver.model.geojson.photo.FeatureCollection.SCHEMA$;
			Decoder decoder = DecoderFactory.get().jsonDecoder(parsedSchema, din);
		    DatumReader<GenericData.Record> reader = new GenericDatumReader(parsedSchema);
			adapter.sendMessage(reader.read(null, decoder), cgorName);
		} catch (CommunicationException cEx) {
			log.error("Error sending large data update message!", cEx);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception Ex) {
			log.error("Error sending large data update message!", Ex);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendPhotoGeoJson -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	

	@ApiOperation(value = "sendLargeDataUpdate", nickname = "sendLargeDataUpdate")
	@RequestMapping(value = "/CISRestAdaptor/sendLargeDataUpdate", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "url", value = "the path where the data can be downloaded", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "dataType", value = "the data type of the data", required = true, dataType = "string", paramType = "query", allowableValues = "msword, ogg, pdf, excel, powerpoint, zip, audio_mpeg, audio_vorbis, image_bmp, image_gif, image_geotiff, image_jpeg, image_png, json, geojson, text_plain, video_mpeg, video_msvideo, video_avi, other"),
			@ApiImplicitParam(name = "title", value = "the title of the update message", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "description", value = "The description of the update message", required = false, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendLargeDataUpdate(	@QueryParam("url") String url,
														@QueryParam("dataType") String dataType,
														@QueryParam("title") String title,
														@RequestBody String description) {
		log.info("--> sendLargeDataUpdate");

		try {
			LargeDataUpdate largeData = new LargeDataUpdate();
			largeData.setUrl(url);
			largeData.setDataType(DataType.valueOf(dataType));
			largeData.setTitle(title);
			largeData.setDescription(description);
			
			adapter.sendMessage(largeData);
		} catch (CommunicationException cEx) {
			log.error("Error sending large data update message!", cEx);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendLargeDataUpdate -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendLargeDataUpdateJson", nickname = "sendLargeDataUpdateJson")
	@RequestMapping(value = "/CISRestAdaptor/sendLargeDataUpdateJson", method = RequestMethod.POST)
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendLargeDataUpdateJson( @RequestBody String requestJson ) {
		System.out.println(requestJson);
		log.info("--> sendLargeDataUpdateJson");

		try {
			JSONObject json = new JSONObject(requestJson);
			LargeDataUpdate largeData = new LargeDataUpdate();
			largeData.setUrl(json.getString("url"));
			largeData.setDataType(DataType.valueOf(json.getString("dataType")));
			largeData.setTitle(json.getString("title"));
			largeData.setDescription(json.getString("description"));
			
			adapter.sendMessage(largeData);
		} catch (CommunicationException cEx) {
			log.error("Error sending large data update message!", cEx);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception Ex) {
			log.error("Error sending large data update message!", Ex);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendLargeDataUpdateJson -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendMapLayerUpdate", nickname = "sendMapLayerUpdate")
	@RequestMapping(value = "/CISRestAdaptor/sendMapLayerUpdate", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "url", value = "the path where the data can be downloaded", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "title", value = "the title of the update message", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "layerType", value = "the type of the layer", required = true, dataType = "string", paramType = "query", allowableValues = "WMS, WMTS, WCS, WFS, OTHER"),
			@ApiImplicitParam(name = "updateType", value = "the type of update", required = true, dataType = "string", paramType = "query", allowableValues = "CREATE, UPDATE, DELETE"),
			@ApiImplicitParam(name = "username", value = "the username for getting the data", required = false, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "password", value = "the password for getting the data", required = false, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "description", value = "The description of the update message", required = false, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendMapLayerUpdate(	@QueryParam("url") String url,
														@QueryParam("title") String title,
														@QueryParam("layerType") String layerType,
														@QueryParam("updateType") String updateType,
														@QueryParam("username") String username,
														@QueryParam("password") String password,
														@RequestBody String description) {
		log.info("--> sendMapLayerUpdate");

		try {
			MapLayerUpdate layerUpdate = new MapLayerUpdate();
			layerUpdate.setUrl(url);
			layerUpdate.setTitle(title);
			layerUpdate.setLayerType(LayerType.valueOf(layerType));
			layerUpdate.setUpdateType(UpdateType.valueOf(updateType));
			layerUpdate.setUsername(username);
			layerUpdate.setPassword(password);
			layerUpdate.setDescription(description);
			
			adapter.sendMessage(layerUpdate);
		} catch (CommunicationException cEx) {
			log.error("Error sending map layer update message!", cEx);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendMapLayerUpdate -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "subscribeOnTopic", nickname = "subscribeOnTopic")
	@RequestMapping(value = "/CISRestAdaptor/subscribeOnTopic", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "topic", value = "the name of the topic to which you want to subscribe", required = true, dataType = "string", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> subscribeOnTopic(@QueryParam("topic") String topic) {
		log.info("--> subscribeOnTopic");
		
		if (this.registeredTopics.get(topic) == null) {
			this.adapter.addCallback(new AdapterCallback(), topic);
			this.registeredTopics.put(topic, topic);
		}
		
		log.info("subscribeOnTopic -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}

}
