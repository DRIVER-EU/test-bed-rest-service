package eu.driver.adaptor.ws.object;

import java.util.Date;

public class WSHeartbeatResponse {
	private String requestId;
	private String type = "eu.driver.adaptor.ws.response.heartbeat";
	private Date sendTime;
	private String state;
	
	public WSHeartbeatResponse() {
		
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
