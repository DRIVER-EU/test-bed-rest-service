package eu.driver.adaptor.callback;

import org.apache.avro.generic.IndexedRecord;
import org.apache.log4j.Logger;

import eu.driver.adaptor.ws.CallbackController;
import eu.driver.api.IAdaptorCallback;

public class AdapterCallback implements IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	private String topicName = null;
	
	public AdapterCallback(String topicName) {
		this.topicName = topicName;
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord message) {
		log.info("-->messageReceived: " + message);
		
		CallbackController.getInstance().sendMessage(topicName, message.toString());
		
		log.info("messageReceived-->");
	}

}
