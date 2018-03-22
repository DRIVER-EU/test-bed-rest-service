package eu.driver.adaptor.ws;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import eu.driver.adaptor.ws.mapper.StringJSONMapper;
import eu.driver.adaptor.ws.object.WSHeartbeatRequest;

public class WebSocketServer extends TextWebSocketHandler {
	
	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	public WebSocketServer() {

	}
	 
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    	log.info("The WebSocket has been closed!");
    	session.close();
    	CallbackController.getInstance().setWSSession(null);
    }
 
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	log.info("The WebSocket has been opened!");
    	CallbackController.getInstance().setWSSession(session);
    	WSHeartbeatRequest hbRequest = new WSHeartbeatRequest();
    	hbRequest.setRequestId("1224-at56-7890-atgf");
    	hbRequest.setSendTime(new Date());
    	
    	TextMessage responseMsg = new TextMessage(mapper.objectToJSONString(hbRequest));
		session.sendMessage(responseMsg);
    }
 
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
    	log.info("Message received: " + textMessage.getPayload());
    	log.info("Date: " + new Date());
    	
    	String message = textMessage.getPayload();
    	if (message.indexOf("eu.driver.adaptor.ws.request.heartbeat") != -1) {
    		WSHeartbeatRequest hbRequest = mapper.stringToHBRequestMessage(textMessage.getPayload());
    		
    		TextMessage responseMsg = new TextMessage(mapper.objectToJSONString(hbRequest.createResponse()));
    		session.sendMessage(responseMsg);
    	}
    	
    }
}
