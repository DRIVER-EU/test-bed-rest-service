package eu.driver.adaptor.ws.heartbeat;

import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import eu.driver.adaptor.ws.mapper.StringJSONMapper;
import eu.driver.adaptor.ws.object.WSHeartbeatRequest;

public class HBTimerTask extends TimerTask {
	
	private Logger log = Logger.getLogger(this.getClass());
	private WebSocketSession session = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	
	public HBTimerTask(WebSocketSession session) {
		log.debug("HBTimerTask");
		this.session = session;
	}

	@Override
	public void run() {
		log.info("HBTimerTask: run");
		// TODO Auto-generated method stub
		WSHeartbeatRequest hbRequest = new WSHeartbeatRequest();
		hbRequest.setRequestId(UUID.randomUUID().toString());
		hbRequest.setSendTime(new Date());
		
		try {
			this.session.sendMessage(new TextMessage(mapper.objectToJSONString(hbRequest)));
		} catch (IOException e) {
			log.error("Error sending Heartbeat message: " + e);
		}

	}

}
