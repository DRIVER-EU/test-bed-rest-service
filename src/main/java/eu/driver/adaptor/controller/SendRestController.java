package eu.driver.adaptor.controller;

import java.util.Date;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.avro.generic.GenericRecord;
import org.apache.log4j.Logger;
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

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.CISAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adaptor.callback.AdapterCallback;
import eu.driver.adaptor.mapper.cap.XMLToAVROMapper;
import eu.driver.model.core.Level;
import eu.driver.model.core.Log;

@RestController
public class SendRestController implements ResourceProcessor<RepositoryLinksResource> {

	private Logger log = Logger.getLogger(this.getClass());
	private XMLToAVROMapper avroMapper = new XMLToAVROMapper();
	private CISAdapter adapter = CISAdapter.getInstance();
 
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SendRestController.class).sendXMLMessage("CAP", "defaultCGOR", "XML")).withRel("sendXMLMessage"));
	    return resource;
	}
	
	public SendRestController() {
		this.adapter.addCallback(new AdapterCallback(), TopicConstants.STANDARD_TOPIC_CAP);
	}
	
	@ApiOperation(value = "sendXMLMessage", nickname = "sendXMLMessage")
	@RequestMapping(value = "/CISRestAdaptor/sendXMLMessage/{type}", method = RequestMethod.POST, consumes = {"appication/xml"} )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "type", value = "the type of the xml content", required = true, dataType = "string", paramType = "path", allowableValues="CAP, MLP, GEOJSON, EMSI"),
        @ApiImplicitParam(name = "cgorName", value = "name of the cgor, if not provided, default public distribution group is used", required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "xmlMsg", value = "the XML message as string", required = true, dataType = "string", paramType = "body", example="<Alert></Alert>")
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Response.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Response.class),
            @ApiResponse(code = 500, message = "Failure", response = Response.class)})
	@Produces({"application/json"})
	public ResponseEntity<Response> sendXMLMessage(	@PathVariable String type,
													@QueryParam("cgorName") String cgorName, 
													@RequestBody String xmlMsg) {
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
			log.info("Processing EMSI message.");
			avroRecord = avroMapper.convertGeoJsonToAvro(xmlMsg);
			
		}
		
		if (avroRecord != null) {
			try {
				adapter.sendMessage(avroRecord);
				response.setMessage("The message was send successfully!");
			} catch(CommunicationException cEx) {
				log.error("Error sending the record!", cEx);
				response.setMessage("Error sending the record!");
				response.setDetails(cEx.getMessage());
				return new ResponseEntity<Response>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response.setMessage("Unknown message type!");
			return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
		}
		
		log.info("sendXMLMessage -->");
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendLogMsg", nickname = "sendLogMsg")
	@RequestMapping(value = "/CISRestAdaptor/sendLogMsg/", method = RequestMethod.POST )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "level", value = "level of the log record", required = true, dataType = "string", paramType = "query", allowableValues="DEBUG, INFO, WARN, ERROR, CRITICAL, SILLY"),
        @ApiImplicitParam(name = "message", value = "level of the log record", required = true, dataType = "string", paramType = "body")
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendLogMsg( 	@QueryParam("level") String level,
												@RequestBody String message) {
		log.info("--> sendLogMsg");
		Boolean send = true;
		
		Log logMsg = new Log();
		logMsg.setDateTimeSent(new Date().getTime());
		logMsg.setId(adapter.getClientID());
		logMsg.setLevel(Level.valueOf(level));
		logMsg.setLog(message);
		
		adapter.addLogEntry(logMsg);
			
		log.info("sendLogMsg -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}


}
