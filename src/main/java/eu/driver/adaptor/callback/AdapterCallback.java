package eu.driver.adaptor.callback;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import eu.driver.adaptor.ws.CallbackController;
import eu.driver.api.IAdaptorCallback;

public class AdapterCallback implements IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	
	public AdapterCallback() {
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord message, String topicName) {
		log.info("-->messageReceived: " + message);
		eu.driver.model.edxl.EDXLDistribution msgKey = (eu.driver.model.edxl.EDXLDistribution) SpecificData.get().deepCopy(eu.driver.model.edxl.EDXLDistribution.SCHEMA$, key);
		String strMsg = message.toString();
		
		try {
			JSONObject jsonMsg = new JSONObject(strMsg);
			CallbackController.getInstance().sendMessage(topicName, msgKey.getSenderID().toString(), message.toString());
		} catch (Exception e) {
			log.error("Error tansforming message to JSON Obejct!", e);
		}
		
		log.info("messageReceived-->");
	}

}
