package eu.driver.adaptor.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class CallbackController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private static CallbackController aMe;
	private WebSocketSession session = null;
	private String restEndpointUrl = null;
	
	private CallbackController() {
		log.info("CallbackController");
	}
	
	public static synchronized CallbackController getInstance() {
		if (CallbackController.aMe == null) {
			CallbackController.aMe = new CallbackController();
		}
		return CallbackController.aMe;
	}
	
	public void setWSSession(WebSocketSession session) {
		log.info("-->setWSSession");
		this.session = session;
		log.info("setWSSession-->");
	}
	
	public void setRestEndpointUrl(String endpointUrl) {
		log.info("-->setRestEndpointUrl");
		this.restEndpointUrl = endpointUrl;
		log.info("setRestEndpointUrl-->");
	}
	
	public void sendMessage(String topic, String msg) {
		log.info("--> sendMessage");
		log.debug(msg);
		
		JSONObject responseJson = new JSONObject();
		try {
			responseJson.put("topic", topic);
			responseJson.put("payload", msg);	
		} catch (Exception e) {
			log.error("Error creating the notification message!");
		}
		
		TextMessage responseMsg = new TextMessage(responseJson.toString());
		try {
			if (session != null && session.isOpen()) {
				session.sendMessage(responseMsg);
			} else if (restEndpointUrl != null) {
				sendMessagetoRESTEndpoint(responseJson.toString());
			}
		} catch (IOException e) {
			log.error("Error sending the notification!");
		}
		log.info("sendMessage -->");
	}
	
	private void sendMessagetoRESTEndpoint(String message) {
		log.info("--> sendMessagetoRESTEndpoint");
		
		postHTTPRequest(restEndpointUrl, "application/json", message);
		
		log.info("sendMessagetoRESTEndpoint -->");
	}
	
	private String postHTTPRequest(String url, String contentType, String msgParam) {
		log.info("--> postHTTPRequest");
		log.debug("url: " + url + ", contentType: " + contentType + ", msgParam: " + msgParam);
		String response = null;
		
		try {
			HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
	        connection.setRequestMethod("POST");
	        connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", contentType);
			if (msgParam != null) {
				byte[] postDataBytes = msgParam.getBytes("UTF-8");
				connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				connection.getOutputStream().write(postDataBytes);
			}
			
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			response = "";
			while ((line = reader.readLine()) != null) {
				response += line;
			}
			
			reader.close();
			
			log.debug("sendMessage: " + response);
	        
		} catch (Exception e) {
			log.error("Error distributing the Message!", e);
			response = null;
		}
		
		return response;
		
	}


}
