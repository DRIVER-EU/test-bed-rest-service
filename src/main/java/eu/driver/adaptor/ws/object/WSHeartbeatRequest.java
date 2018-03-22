package eu.driver.adaptor.ws.object;

import java.util.Date;

public class WSHeartbeatRequest {
	private String requestId;
	private String type = "eu.driver.adaptor.ws.request.heartbeat";
	private Date sendTime;
	
	public WSHeartbeatRequest() {
		
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	
	public WSHeartbeatResponse createResponse() {
		WSHeartbeatResponse response = new WSHeartbeatResponse();
		
		response.setRequestId(this.requestId);
		response.setSendTime(new Date());
		response.setState("OK");
		
		return response;
	}
}
