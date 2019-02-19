package eu.driver.adaptor.callback;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
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
		eu.driver.model.edxl.EDXLDistribution msgKey = (eu.driver.model.edxl.EDXLDistribution) SpecificData.get().deepCopy(eu.driver.model.edxl.EDXLDistribution.SCHEMA$, key);
		CallbackController.getInstance().sendMessage(topicName, msgKey.getSenderID().toString(), message.toString());
		
		log.info("messageReceived-->");
	}

}
