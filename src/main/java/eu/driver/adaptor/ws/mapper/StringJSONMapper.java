package eu.driver.adaptor.ws.mapper;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.driver.adaptor.ws.object.WSHeartbeatRequest;
import eu.driver.adaptor.ws.object.WSHeartbeatResponse;


public class StringJSONMapper {
	
	private Logger log = Logger.getLogger(this.getClass());
	private ObjectMapper mapper = null;
	
	public StringJSONMapper() {
		log.debug("StringJSONMapper");
		mapper = new ObjectMapper();
	}
	
	public String objectToJSONString(Object object) {
		log.info("--> objectToJSONString");
    	String jsonInString = null;
    	
    	if (this.mapper != null) {
    		try {
    			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    		} catch (JsonGenerationException e) {
    			log.error("Error mapping the message!", e);
    		} catch (JsonMappingException e) {
    			log.error("Error mapping the message!", e);
    		} catch (IOException e) {
    			log.error("Error mapping the message!", e);
    		}
    	}
    	log.debug("objectToJSONString --> " + jsonInString);
    	return jsonInString;
    }
	
	public WSHeartbeatResponse stringToHBResponseMessage(String message) {
		log.info("--> stringToHBResponseMessage");
		WSHeartbeatResponse response = null;
		try {
			response = mapper.readValue(message, WSHeartbeatResponse.class);
		} catch (JsonParseException e) {
			log.error("Error reading the message!", e);
		} catch (JsonMappingException e) {
			log.error("Error reading the message!", e);
		} catch (IOException e) {
			log.error("Error reading the message!", e);
		}
		
		return response;
	}
	
	public WSHeartbeatRequest stringToHBRequestMessage(String message) {
		log.info("--> stringToHBRequestMessage");
		WSHeartbeatRequest request = null;
		try {
			request = mapper.readValue(message, WSHeartbeatRequest.class);
		} catch (JsonParseException e) {
			log.error("Error reading the message!", e);
		} catch (JsonMappingException e) {
			log.error("Error reading the message!", e);
		} catch (IOException e) {
			log.error("Error reading the message!", e);
		}
		
		return request;
	}

}
